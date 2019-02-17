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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static se.omfilm.gameboy.util.DebugPrinter.hex;

public class DebugGUI {
    private final Debugger debugger;

    private List<ValueChangeUpdaters<Void>> changeUpdaters = new ArrayList<>();
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

    private void addMemoryReadBreakpoint() {
        try {
            String value = TextInputDialog.showDialog(gui, "Add Memory Read Breakpoint", "Memory address break on (0xXXXX)", "");
            if (value == null) {
                return;
            }
            Pattern pattern = Pattern.compile("(0x)?([0-9A-F]{4})");
            Matcher matcher = pattern.matcher(value);
            if (!matcher.matches()) {
                throw new IllegalArgumentException(value + " isn't a valid memory address");
            }
            String address = matcher.group(2);
            debugger.addReadBreakpoint(Integer.parseInt(address, 16));
            reloadBreakpoints();
        } catch (Exception e) {
            addProgramCounterBreakpoint();
        }
    }

    private void addMemoryWriteBreakpoint() {
        try {
            String value = TextInputDialog.showDialog(gui, "Add Memory Write Breakpoint", "Memory address break on (0xXXXX)", "");
            if (value == null) {
                return;
            }
            Pattern pattern = Pattern.compile("(0x)?([0-9A-F]{4})");
            Matcher matcher = pattern.matcher(value);
            if (!matcher.matches()) {
                throw new IllegalArgumentException(value + " isn't a valid memory address");
            }
            String address = matcher.group(2);
            debugger.addWriteBreakpoint(Integer.parseInt(address, 16));
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

        for (ValueChangeUpdaters updater : changeUpdaters) {
            updater.update(currentState);
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
        readOnlyTextBox(sound1Panel, "Length (0xFF11)", (currentState) -> ioString(currentState.apu().soundStates()[0].length()));
        readOnlyTextBox(sound1Panel, "Envelope (0xFF12)", (currentState) -> ioString(currentState.apu().soundStates()[0].envelope()));
        readOnlyTextBox(sound1Panel, "Low freq (0xFF13)", (currentState) -> ioString(currentState.apu().soundStates()[0].lowFrequency()));
        readOnlyTextBox(sound1Panel, "Hi freq (0xFF14)", (currentState) -> ioString(currentState.apu().soundStates()[0].highFrequency()));
        readOnlyTextBox(sound1Panel, "Sweep (0xFF10)", (currentState) -> ioString(currentState.apu().soundStates()[0].sweep()));

        Panel sound2Panel = createSoundPanel();
        readOnlyTextBox(sound2Panel, "Length (0xFF16)", (currentState) -> ioString(currentState.apu().soundStates()[1].length()));
        readOnlyTextBox(sound2Panel, "Envelope (0xFF17)", (currentState) -> ioString(currentState.apu().soundStates()[1].envelope()));
        readOnlyTextBox(sound2Panel, "Low freq (0xFF18)", (currentState) -> ioString(currentState.apu().soundStates()[1].lowFrequency()));
        readOnlyTextBox(sound2Panel, "Hi freq (0xFF19)", (currentState) -> ioString(currentState.apu().soundStates()[1].highFrequency()));
        Panel sound3Panel = createSoundPanel();
        readOnlyTextBox(sound3Panel, "Length (0xFF1B)", (currentState) -> ioString(currentState.apu().soundStates()[2].length()));
        readOnlyTextBox(sound3Panel, "Low freq (0xFF1D)", (currentState) -> ioString(currentState.apu().soundStates()[2].lowFrequency()));
        readOnlyTextBox(sound3Panel, "Hi freq (0xFF1E)", (currentState) -> ioString(currentState.apu().soundStates()[2].highFrequency()));
        readOnlyTextBox(sound3Panel, "On/Off (0xFF1A)", (currentState) -> ioString(currentState.apu().soundStates()[2].onOff()));
        readOnlyTextBox(sound3Panel, "Out lvl (0xFF1C)", (currentState) -> ioString(currentState.apu().soundStates()[2].outputLevel()));
        Panel sound4Panel = createSoundPanel();
        readOnlyTextBox(sound4Panel, "Length (0xFF20)", (currentState) -> ioString(currentState.apu().soundStates()[3].length()));
        readOnlyTextBox(sound4Panel, "Envelope (0xFF21)", (currentState) -> ioString(currentState.apu().soundStates()[3].envelope()));
        readOnlyTextBox(sound4Panel, "Poly (0xFF22)", (currentState) -> ioString(currentState.apu().soundStates()[3].polynomial()));
        readOnlyTextBox(sound4Panel, "Initial (0xFF23)", (currentState) -> ioString(currentState.apu().soundStates()[3].initial()));
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
        for (int i = 0; i < 16; i++) {
            final int j = i;
            readOnlyTextBox(panel, DebugPrinter.hex(0xFF30 + i, 4), (currentState) -> hex(currentState.apu().waveRAM()[j], 2));
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
        readOnlyTextBox(panel, "Enabled (0xFF26)", (currentState) -> ioString(currentState.apu().enabled()));
        readOnlyTextBox(panel, "Terminal (0xFF25)", (currentState) -> ioString(currentState.apu().outputTerminal()));
        readOnlyTextBox(panel, "Chn Ctrl (0xFF24)", (currentState) -> ioString(currentState.apu().channelControl()));
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
        panel.addComponent(new Button("+Read break", this::addMemoryReadBreakpoint));
        panel.addComponent(new Button("+Write break", this::addMemoryWriteBreakpoint));
        panel.addComponent(new Button("Quit", this::quit));

        return panel.withBorder(Borders.singleLine("Actions"));
    }

    private Component createRegistersPanel() {
        Panel panel = new Panel();
        panel.setLayoutManager(new GridLayout(2));

        readOnlyTextBox(panel, "A", (currentState) -> hex(currentState.registers().readA(), 2));
        readOnlyTextBox(panel, "B", (currentState) -> hex(currentState.registers().readB(), 2));
        readOnlyTextBox(panel, "C", (currentState) -> hex(currentState.registers().readC(), 2));
        readOnlyTextBox(panel, "D", (currentState) -> hex(currentState.registers().readD(), 2));
        readOnlyTextBox(panel, "E", (currentState) -> hex(currentState.registers().readE(), 2));
        readOnlyTextBox(panel, "F", (currentState) -> hex(currentState.registers().readF(), 2));
        readOnlyTextBox(panel, "H", (currentState) -> hex(currentState.registers().readH(), 2));
        readOnlyTextBox(panel, "L", (currentState) -> hex(currentState.registers().readL(), 2));
        readOnlyTextBox(panel, "AF", (currentState) -> hex(currentState.registers().readAF(), 4));
        readOnlyTextBox(panel, "BC", (currentState) -> hex(currentState.registers().readBC(), 4));
        readOnlyTextBox(panel, "DE", (currentState) -> hex(currentState.registers().readDE(), 4));
        readOnlyTextBox(panel, "HL", (currentState) -> hex(currentState.registers().readHL(), 4));

        return panel.withBorder(Borders.singleLine("Registers"));
    }

    private Component createBreakpointsPanel() {
        Panel panel = new Panel();
        panel.setLayoutManager(new LinearLayout());
        panel.addComponent(breakPoints = new CheckBoxList<>());
        breakPoints.addListener((i, b) -> removeBreakpoint(i));
        reloadBreakpoints();

        return panel.withBorder(Borders.singleLine("Breakpoints"));
    }

    private Component createInterruptsPanel() {
        Panel panel = new Panel();
        panel.setLayoutManager(new GridLayout(3));

        panel.addComponent(new EmptySpace());
        panel.addComponent(new Label("Enabled"));
        panel.addComponent(new Label("Requested"));

        readOnlyCheckBox(panel, "MASTER", (currentState) -> !currentState.flags().isInterruptsDisabled());
        panel.addComponent(new EmptySpace());
        readOnlyCheckBox(panel, "VBLANK", (currentState) -> currentState.interrupts().enabled(Interrupts.Interrupt.VBLANK));
        readOnlyCheckBox(panel, null, (currentState) -> currentState.interrupts().requested(Interrupts.Interrupt.VBLANK));

        readOnlyCheckBox(panel, "LCD", (currentState) -> currentState.interrupts().enabled(Interrupts.Interrupt.LCD));
        readOnlyCheckBox(panel, null, (currentState) -> currentState.interrupts().requested(Interrupts.Interrupt.LCD));

        readOnlyCheckBox(panel, "TIMER", (currentState) -> currentState.interrupts().enabled(Interrupts.Interrupt.TIMER));
        readOnlyCheckBox(panel, null, (currentState) -> currentState.interrupts().requested(Interrupts.Interrupt.TIMER));

        readOnlyCheckBox(panel, "SERIAL", (currentState) -> currentState.interrupts().enabled(Interrupts.Interrupt.SERIAL));
        readOnlyCheckBox(panel, null, (currentState) -> currentState.interrupts().requested(Interrupts.Interrupt.SERIAL));

        readOnlyCheckBox(panel, "JOYPAD", (currentState) -> currentState.interrupts().enabled(Interrupts.Interrupt.JOYPAD));
        readOnlyCheckBox(panel, null, (currentState) -> currentState.interrupts().requested(Interrupts.Interrupt.JOYPAD));

        return panel.withBorder(Borders.singleLine("Interrupts"));
    }

    private Component createFlagsPanel() {
        Panel panel = new Panel();
        panel.setLayoutManager(new GridLayout(2));

        readOnlyCheckBox(panel, "ZERO", (currentState) -> currentState.flags().isSet(Flags.Flag.ZERO));
        readOnlyCheckBox(panel, "SUBTRACT", (currentState) -> currentState.flags().isSet(Flags.Flag.SUBTRACT));
        readOnlyCheckBox(panel, "CARRY", (currentState) -> currentState.flags().isSet(Flags.Flag.CARRY));
        readOnlyCheckBox(panel, "HALF CARRY", (currentState) -> currentState.flags().isSet(Flags.Flag.HALF_CARRY));

        return panel.withBorder(Borders.singleLine("Flags"));
    }

    private Component createGeneralCounterPanel() {
        Panel panel = new Panel();
        panel.setLayoutManager(new GridLayout(2));

        readOnlyTextBox(panel, "Program Counter", (currentState) -> hex(currentState.programCounter().read(), 4));
        readOnlyTextBox(panel, "Instruction", (currentState) -> Optional.ofNullable(currentState.instructionType()).map(Enum::name).orElse("")); //TODO: this may not be correct since it can return null
        readOnlyTextBox(panel, "Stack Pointer", (currentState) -> hex(currentState.stackPointer().read(), 4));

        return panel.withBorder(Borders.singleLine("General"));
    }

    private void readOnlyTextBox(Panel panel, String label, ValueChangeUpdaters<String> listener) {
        TextBox textBox = new HighlightableTextBox();
        textBox.setEnabled(false);
        panel.addComponent(new Label(label));
        panel.addComponent(textBox);
        changeUpdaters.add((currentState) -> {
            textBox.setText(listener.update(currentState));
            return null;
        });
    }

    private void readOnlyCheckBox(Panel panel, String label, ValueChangeUpdaters<Boolean> listener) {
        CheckBox checkBox = new HighlightableCheckBox();
        checkBox.setEnabled(false);
        if (label != null) {
            panel.addComponent(new Label(label));
        }
        panel.addComponent(checkBox);
        changeUpdaters.add((currentState) -> {
           checkBox.setChecked(listener.update(currentState));
           return null;
        });
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

    private interface ValueChangeUpdaters<T> {
        T update(EmulatorState state);
    }
}
