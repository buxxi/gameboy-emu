package se.omfilm.gameboy.debug;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.bundle.LanternaThemes;
import com.googlecode.lanterna.graphics.Theme;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.dialogs.TextInputDialog;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import se.omfilm.gameboy.debug.EmulatorState.IORegisterState;
import se.omfilm.gameboy.internal.Flags;
import se.omfilm.gameboy.internal.Instruction;
import se.omfilm.gameboy.internal.Interrupts;
import se.omfilm.gameboy.util.DebugPrinter;

import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static se.omfilm.gameboy.util.DebugPrinter.hex;

public class DebugGUI {
    private final Debugger debugger;

    private TextBox programCounter;
    private TextBox instruction;

    private TextBox stackPointer;

    private CheckBox zeroFlag;
    private CheckBox subtractFlag;
    private CheckBox carryFlag;
    private CheckBox halfCarryFlag;

    private CheckBox interruptsEnabled;
    private CheckBox vblankEnabled;
    private CheckBox vblankRequested;
    private CheckBox lcdEnabled;
    private CheckBox lcdRequested;
    private CheckBox timerEnabled;
    private CheckBox timerRequested;
    private CheckBox serialEnabled;
    private CheckBox serialRequested;
    private CheckBox joypadEnabled;
    private CheckBox joypadRequested;

    private TextBox registerA;
    private TextBox registerB;
    private TextBox registerC;
    private TextBox registerD;
    private TextBox registerE;
    private TextBox registerF;
    private TextBox registerH;
    private TextBox registerL;
    private TextBox registerAF;
    private TextBox registerBC;
    private TextBox registerDE;
    private TextBox registerHL;

    private TextBox soundEnabled;
    private TextBox soundOutputTerminal;
    private TextBox soundChannelControl;

    private TextBox sound1Length;
    private TextBox sound2Length;
    private TextBox sound3Length;
    private TextBox sound4Length;

    private TextBox sound1Envelope;
    private TextBox sound2Envelope;
    private TextBox sound4Envelope;

    private TextBox sound1LowFrequency;
    private TextBox sound2LowFrequency;
    private TextBox sound3LowFrequency;

    private TextBox sound1HighFrequency;
    private TextBox sound2HighFrequency;
    private TextBox sound3HighFrequency;

    private TextBox sound1Sweep;

    private TextBox sound3OnOff;
    private TextBox sound3OutputLevel;

    private TextBox sound4Polynomial;
    private TextBox sound4Initial;

    private TextBox[] waveRAM;

    private CheckBoxList<String> breakPoints;

    private boolean needsRedraw = false;

    private Theme normalTheme = LanternaThemes.getRegisteredTheme("blaster");
    private Theme highlightTheme = LanternaThemes.getRegisteredTheme("defrost");
    private MultiWindowTextGUI gui;

    public DebugGUI(Debugger debugger) {
        this.debugger = debugger;
    }

    public void start() {
        new Thread(this::run).start();
    }

    public void update() {
        needsRedraw = true;
    }

    private void pause() {
        debugger.pause();
        reloadBreakpoints();
    }

    private void step() {
        debugger.step();
    }

    private void addInstructionBreakpoint() {
        try {
            String name = TextInputDialog.showDialog(gui, "Add Instruction Breakpoint", "Name of the instruction to break on", "");
            if (name == null) {
                return;
            }
            debugger.addBreakpoint(Instruction.InstructionType.valueOf(name));
            reloadBreakpoints();
        } catch (Exception e) {
            addInstructionBreakpoint();
        }
    }

    private void addProgramCounterBreakpoint() {
        try {
            String value = TextInputDialog.showDialog(gui, "Add Program Counter Breakpoint", "Program Counter value to break on (0xXXXX)", "");
            if (value == null) {
                return;
            }
            Pattern pattern = Pattern.compile("(0x)?([0-9A-F]{4})");
            Matcher matcher = pattern.matcher(value);
            if (!matcher.matches()) {
                throw new IllegalArgumentException(value + " isn't a valid program counter");
            }
            String address = matcher.group(2);
            debugger.addBreakpoint(Integer.parseInt(address, 16));
            reloadBreakpoints();
        } catch (Exception e) {
            addProgramCounterBreakpoint();
        }
    }

    private void removeBreakpoint(int index) {
        debugger.removeBreakpoint(debugger.getBreakpoints().get(index));
        reloadBreakpoints();
    }

    private void quit() {
        System.exit(0);
    }

    private void reloadBreakpoints() {
        breakPoints.clearItems();
        for (Breakpoint breakpoint : debugger.getBreakpoints()) {
            breakPoints.addItem(breakpoint.displayText(), true);
        }
    }

    private void redrawComponents() {
        if (!needsRedraw) {
            return;
        }
        needsRedraw = false;

        EmulatorState currentState = debugger.getCurrentState();

        programCounter.setText(hex(currentState.programCounter().read(), 4));
        instruction.setText(Optional.ofNullable(currentState.instructionType()).map(Enum::name).orElse("")); //TODO: this may not be correct since it can return null
        stackPointer.setText(hex(currentState.stackPointer().read(), 4));

        zeroFlag.setChecked(currentState.flags().isSet(Flags.Flag.ZERO));
        subtractFlag.setChecked(currentState.flags().isSet(Flags.Flag.SUBTRACT));
        carryFlag.setChecked(currentState.flags().isSet(Flags.Flag.CARRY));
        halfCarryFlag.setChecked(currentState.flags().isSet(Flags.Flag.HALF_CARRY));

        interruptsEnabled.setChecked(!currentState.flags().isInterruptsDisabled());
        vblankEnabled.setChecked(currentState.interrupts().enabled(Interrupts.Interrupt.VBLANK));
        vblankRequested.setChecked(currentState.interrupts().requested(Interrupts.Interrupt.VBLANK));
        lcdEnabled.setChecked(currentState.interrupts().enabled(Interrupts.Interrupt.LCD));
        lcdRequested.setChecked(currentState.interrupts().requested(Interrupts.Interrupt.LCD));
        timerEnabled.setChecked(currentState.interrupts().enabled(Interrupts.Interrupt.TIMER));
        timerRequested.setChecked(currentState.interrupts().requested(Interrupts.Interrupt.TIMER));
        serialEnabled.setChecked(currentState.interrupts().enabled(Interrupts.Interrupt.SERIAL));
        serialRequested.setChecked(currentState.interrupts().requested(Interrupts.Interrupt.SERIAL));
        joypadEnabled.setChecked(currentState.interrupts().enabled(Interrupts.Interrupt.JOYPAD));
        joypadRequested.setChecked(currentState.interrupts().requested(Interrupts.Interrupt.JOYPAD));

        registerA.setText(hex(currentState.registers().readA(), 2));
        registerB.setText(hex(currentState.registers().readB(), 2));
        registerC.setText(hex(currentState.registers().readC(), 2));
        registerD.setText(hex(currentState.registers().readD(), 2));
        registerE.setText(hex(currentState.registers().readE(), 2));
        registerF.setText(hex(currentState.registers().readF(), 2));
        registerH.setText(hex(currentState.registers().readH(), 2));
        registerL.setText(hex(currentState.registers().readL(), 2));
        registerAF.setText(hex(currentState.registers().readAF(), 4));
        registerBC.setText(hex(currentState.registers().readBC(), 4));
        registerDE.setText(hex(currentState.registers().readDE(), 4));
        registerHL.setText(hex(currentState.registers().readHL(), 4));

        soundEnabled.setText(ioString(currentState.apu().enabled()));
        soundOutputTerminal.setText(ioString(currentState.apu().outputTerminal()));
        soundChannelControl.setText(ioString(currentState.apu().channelControl()));

        sound1Length.setText(ioString(currentState.apu().soundStates()[0].length()));
        sound2Length.setText(ioString(currentState.apu().soundStates()[1].length()));
        sound3Length.setText(ioString(currentState.apu().soundStates()[2].length()));
        sound4Length.setText(ioString(currentState.apu().soundStates()[3].length()));

        sound1Envelope.setText(ioString(currentState.apu().soundStates()[0].envelope()));
        sound2Envelope.setText(ioString(currentState.apu().soundStates()[1].envelope()));
        sound4Envelope.setText(ioString(currentState.apu().soundStates()[3].envelope()));

        sound1LowFrequency.setText(ioString(currentState.apu().soundStates()[0].lowFrequency()));
        sound2LowFrequency.setText(ioString(currentState.apu().soundStates()[1].lowFrequency()));
        sound3LowFrequency.setText(ioString(currentState.apu().soundStates()[2].lowFrequency()));

        sound1HighFrequency.setText(ioString(currentState.apu().soundStates()[0].highFrequency()));
        sound2HighFrequency.setText(ioString(currentState.apu().soundStates()[1].highFrequency()));
        sound3HighFrequency.setText(ioString(currentState.apu().soundStates()[2].highFrequency()));

        sound1Sweep.setText(ioString(currentState.apu().soundStates()[0].sweep()));
        sound3OnOff.setText(ioString(currentState.apu().soundStates()[2].onOff()));
        sound3OutputLevel.setText(ioString(currentState.apu().soundStates()[2].outputLevel()));
        sound4Polynomial.setText(ioString(currentState.apu().soundStates()[3].polynomial()));
        sound4Initial.setText(ioString(currentState.apu().soundStates()[3].initial()));

        for (int i = 0; i < waveRAM.length; i++) {
            waveRAM[i].setText(hex(currentState.apu().waveRAM()[i], 2));
        }
    }

    private String ioString(IORegisterState state) {
        return hex(state.written(), 2) + "/" + hex(state.read(), 2);
    }

    private Component createPanel() {
        Panel mainPanel = new Panel();
        mainPanel.setLayoutManager(new LinearLayout(Direction.HORIZONTAL));

        Panel rightPanel = new Panel();
        rightPanel.setLayoutManager(new LinearLayout());
        rightPanel.addComponent(createButtonsPanel());
        rightPanel.addComponent(createBreakpointsPanel());

        mainPanel.addComponent(createCPUPanel());
        mainPanel.addComponent(createAPUPanel());
        mainPanel.addComponent(rightPanel);

        return mainPanel;
    }

    private Component createAPUPanel() {
        Panel panel = new Panel();
        panel.setLayoutManager(new LinearLayout());
        panel.addComponent(createGeneralAPUPanel());
        Panel soundPanel = new Panel();
        soundPanel.setLayoutManager(new LinearLayout(Direction.HORIZONTAL));
        Panel sound1Panel = createSoundPanel();
        sound1Length = readOnlyTextBox(sound1Panel, "Length");
        sound1Envelope = readOnlyTextBox(sound1Panel, "Envelope");
        sound1LowFrequency = readOnlyTextBox(sound1Panel, "Low freq");
        sound1HighFrequency = readOnlyTextBox(sound1Panel, "Hi freq");
        sound1Sweep = readOnlyTextBox(sound1Panel, "Sweep");
        Panel sound2Panel = createSoundPanel();
        sound2Length = readOnlyTextBox(sound2Panel, "Length");
        sound2Envelope = readOnlyTextBox(sound2Panel, "Envelope");
        sound2LowFrequency = readOnlyTextBox(sound2Panel, "Low freq");
        sound2HighFrequency = readOnlyTextBox(sound2Panel, "Hi freq");
        Panel sound3Panel = createSoundPanel();
        sound3Length = readOnlyTextBox(sound3Panel, "Length");
        sound3LowFrequency = readOnlyTextBox(sound3Panel, "Low freq");
        sound3HighFrequency = readOnlyTextBox(sound3Panel, "Hi freq");
        sound3OnOff = readOnlyTextBox(sound3Panel, "On/Off");
        sound3OutputLevel = readOnlyTextBox(sound3Panel, "Out lvl");
        Panel sound4Panel = createSoundPanel();
        sound4Length = readOnlyTextBox(sound4Panel, "Length");
        sound4Envelope = readOnlyTextBox(sound4Panel, "Envelope");
        sound4Polynomial = readOnlyTextBox(sound4Panel, "Poly");
        sound4Initial = readOnlyTextBox(sound4Panel, "Initial");
        soundPanel.addComponent(sound1Panel.withBorder(Borders.singleLine("Sound 1")));
        soundPanel.addComponent(sound2Panel.withBorder(Borders.singleLine("Sound 2")));
        soundPanel.addComponent(sound3Panel.withBorder(Borders.singleLine("Sound 3")));
        soundPanel.addComponent(sound4Panel.withBorder(Borders.singleLine("Sound 4")));
        panel.addComponent(soundPanel);
        panel.addComponent(createWaveRAMPanel());
        return panel.withBorder(Borders.singleLine("APU"));
    }

    private Component createWaveRAMPanel() {
        Panel panel = new Panel();
        panel.setLayoutManager(new GridLayout(8));
        waveRAM = new TextBox[16];
        for (int i = 0; i < waveRAM.length; i++) {
            waveRAM[i] = readOnlyTextBox(panel, DebugPrinter.hex(0xFF30 + i, 4));
        }
        return panel.withBorder(Borders.singleLine("Wave RAM"));
    }

    private Panel createSoundPanel() {
        Panel panel = new Panel();
        panel.setLayoutManager(new GridLayout(2));
        return panel;
    }

    private Component createGeneralAPUPanel() {
        Panel panel = new Panel();
        panel.setLayoutManager(new LinearLayout(Direction.HORIZONTAL));
        soundEnabled = readOnlyTextBox(panel, "Enabled (0xFF26)");
        soundOutputTerminal = readOnlyTextBox(panel, "Terminal (0xFF25)");
        soundChannelControl = readOnlyTextBox(panel, "Chn Ctrl (0xFF24)");
        return panel.withBorder(Borders.singleLine("General"));
    }

    private Component createCPUPanel() {
        Panel panel = new Panel();
        panel.setLayoutManager(new LinearLayout());
        panel.addComponent(createGeneralCounterPanel());
        panel.addComponent(createFlagsPanel());
        panel.addComponent(createInterruptsPanel());
        panel.addComponent(createRegistersPanel());
        return panel.withBorder(Borders.singleLine("CPU"));
    }

    private Component createButtonsPanel() {
        Panel panel = new Panel();
        panel.setLayoutManager(new LinearLayout());
        panel.addComponent(new Button("Break", this::pause));
        panel.addComponent(new Button("Step", this::step));
        panel.addComponent(new Button("+Instr break", this::addInstructionBreakpoint));
        panel.addComponent(new Button("+PC break", this::addProgramCounterBreakpoint));
        panel.addComponent(new Button("Quit", this::quit));

        return panel.withBorder(Borders.singleLine("Actions"));
    }

    private Component createRegistersPanel() {
        Panel panel = new Panel();
        panel.setLayoutManager(new GridLayout(2));

        registerA = readOnlyTextBox(panel, "A");
        registerB = readOnlyTextBox(panel, "B");
        registerC = readOnlyTextBox(panel, "C");
        registerD = readOnlyTextBox(panel, "D");
        registerE = readOnlyTextBox(panel, "E");
        registerF = readOnlyTextBox(panel, "F");
        registerH = readOnlyTextBox(panel, "H");
        registerL = readOnlyTextBox(panel, "L");
        registerAF = readOnlyTextBox(panel, "AF");
        registerBC = readOnlyTextBox(panel, "BC");
        registerDE = readOnlyTextBox(panel, "DE");
        registerHL = readOnlyTextBox(panel, "HL");

        return panel.withBorder(Borders.singleLine("Registers"));
    }

    private Component createBreakpointsPanel() {
        Panel panel = new Panel();
        panel.setLayoutManager(new LinearLayout());
        panel.addComponent(breakPoints = new CheckBoxList<>());
        breakPoints.addListener((i, b) -> removeBreakpoint(i));

        return panel.withBorder(Borders.singleLine("Breakpoints"));
    }

    private Component createInterruptsPanel() {
        Panel panel = new Panel();
        panel.setLayoutManager(new GridLayout(3));

        panel.addComponent(new EmptySpace());
        panel.addComponent(new Label("Enabled"));
        panel.addComponent(new Label("Requested"));

        interruptsEnabled = readOnlyCheckBox(panel, "MASTER");
        panel.addComponent(new EmptySpace());
        vblankEnabled = readOnlyCheckBox(panel, "VBLANK");
        vblankRequested = readOnlyCheckBox(panel, null);

        lcdEnabled = readOnlyCheckBox(panel, "LCD");
        lcdRequested = readOnlyCheckBox(panel, null);

        timerEnabled = readOnlyCheckBox(panel, "TIMER");
        timerRequested = readOnlyCheckBox(panel, null);

        serialEnabled = readOnlyCheckBox(panel, "SERIAL");
        serialRequested = readOnlyCheckBox(panel, null);

        joypadEnabled = readOnlyCheckBox(panel, "JOYPAD");
        joypadRequested = readOnlyCheckBox(panel, null);

        return panel.withBorder(Borders.singleLine("Interrupts"));
    }

    private Component createFlagsPanel() {
        Panel panel = new Panel();
        panel.setLayoutManager(new GridLayout(2));

        zeroFlag = readOnlyCheckBox(panel, "ZERO");
        subtractFlag = readOnlyCheckBox(panel, "SUBTRACT");
        carryFlag = readOnlyCheckBox(panel, "CARRY");
        halfCarryFlag = readOnlyCheckBox(panel, "HALF CARRY");

        return panel.withBorder(Borders.singleLine("Flags"));
    }

    private Component createGeneralCounterPanel() {
        Panel panel = new Panel();
        panel.setLayoutManager(new GridLayout(2));

        programCounter = readOnlyTextBox(panel, "Program Counter");
        instruction = readOnlyTextBox(panel, "Instruction");
        stackPointer = readOnlyTextBox(panel, "Stack Pointer");

        return panel.withBorder(Borders.singleLine("General"));
    }

    private TextBox readOnlyTextBox(Panel panel, String label) {
        TextBox textBox = new HighlightableTextBox();
        textBox.setEnabled(false);
        panel.addComponent(new Label(label));
        panel.addComponent(textBox);
        return textBox;
    }

    private CheckBox readOnlyCheckBox(Panel panel, String label) {
        CheckBox checkBox = new HighlightableCheckBox();
        checkBox.setEnabled(false);
        if (label != null) {
            panel.addComponent(new Label(label));
        }
        panel.addComponent(checkBox);
        return checkBox;
    }

    private void run() {
        try {
            Terminal terminal = new DefaultTerminalFactory().setInitialTerminalSize(new TerminalSize(235, 60)).createTerminal();
            Screen screen = new TerminalScreen(terminal);
            screen.startScreen();

            BasicWindow window = new BasicWindow();
            window.setComponent(createPanel());

            gui = new MultiWindowTextGUI(screen, new DefaultWindowManager(), new EmptySpace(TextColor.ANSI.BLUE));
            gui.setTheme(normalTheme);
            gui.addWindow(window);
            while (true) {
                gui.getGUIThread().processEventsAndUpdate();
                redrawComponents();
                gui.updateScreen();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class HighlightableTextBox extends TextBox {
        public synchronized TextBox setText(String newText) {
            if (getLineCount() == 0 || Objects.equals(getTextOrDefault(""), newText)) {
                setTheme(normalTheme);
            } else {
                setTheme(highlightTheme);
            }
            return super.setText(newText);
        }
    }

    private class HighlightableCheckBox extends CheckBox {
        public synchronized CheckBox setChecked(boolean checked) {
            if (checked != isChecked()) {
                setTheme(highlightTheme);
            } else {
                setTheme(normalTheme);
            }
            return super.setChecked(checked);
        }
    }
}
