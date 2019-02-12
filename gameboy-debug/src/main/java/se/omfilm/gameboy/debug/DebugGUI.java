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
import se.omfilm.gameboy.internal.Flags;
import se.omfilm.gameboy.internal.Instruction;
import se.omfilm.gameboy.internal.Interrupts;

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
    }

    private Component createPanel() {
        Panel mainPanel = new Panel();
        mainPanel.setLayoutManager(new GridLayout(3));

        Panel leftPanel = new Panel();
        leftPanel.setLayoutManager(new LinearLayout());
        leftPanel.addComponent(createGeneralCounterPanel());
        leftPanel.addComponent(createFlagsPanel());
        leftPanel.addComponent(createInterruptsPanel());

        Panel middlePanel = new Panel();
        middlePanel.setLayoutManager(new LinearLayout());
        middlePanel.addComponent(createRegistersPanel());

        Panel rightPanel = new Panel();
        rightPanel.setLayoutManager(new LinearLayout());
        rightPanel.addComponent(createButtonsPanel());
        rightPanel.addComponent(createBreakpointsPanel());

        mainPanel.addComponent(leftPanel);
        mainPanel.addComponent(middlePanel);
        mainPanel.addComponent(rightPanel);

        return mainPanel;
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
