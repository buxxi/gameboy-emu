package se.omfilm.gameboy.debug;

import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import se.omfilm.gameboy.internal.Flags;
import se.omfilm.gameboy.internal.Interrupts;

import java.io.IOException;
import java.util.Optional;

import static se.omfilm.gameboy.util.DebugPrinter.hex;

public class DebugGUI {
    private TextBox programCounter;
    private TextBox instruction;

    private TextBox stackPointer;

    private CheckBox zeroFlag;
    private CheckBox subtractFlag;
    private CheckBox carryFlag;
    private CheckBox halfCarryFlag;

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

    private EmulatorState lastState;

    public void start() {
        new Thread(this::run).start();
    }

    public void update(EmulatorState state) {
        lastState = state;
    }

    private void redraw(MultiWindowTextGUI gui) throws IOException {
        if (lastState == null) {
            return;
        }

        programCounter.setText(hex(lastState.programCounter().read(), 4));
        instruction.setText(Optional.ofNullable(lastState.instructionType()).map(Enum::name).orElse("")); //TODO: this may not be correct since it can return null

        stackPointer.setText(hex(lastState.stackPointer().read(), 4));

        zeroFlag.setChecked(lastState.flags().isSet(Flags.Flag.ZERO));
        subtractFlag.setChecked(lastState.flags().isSet(Flags.Flag.SUBTRACT));
        carryFlag.setChecked(lastState.flags().isSet(Flags.Flag.CARRY));
        halfCarryFlag.setChecked(lastState.flags().isSet(Flags.Flag.HALF_CARRY));

        vblankEnabled.setChecked(lastState.interrupts().enabled(Interrupts.Interrupt.VBLANK));
        vblankRequested.setChecked(lastState.interrupts().requested(Interrupts.Interrupt.VBLANK));
        lcdEnabled.setChecked(lastState.interrupts().enabled(Interrupts.Interrupt.LCD));
        lcdRequested.setChecked(lastState.interrupts().requested(Interrupts.Interrupt.LCD));
        timerEnabled.setChecked(lastState.interrupts().enabled(Interrupts.Interrupt.TIMER));
        timerRequested.setChecked(lastState.interrupts().requested(Interrupts.Interrupt.TIMER));
        serialEnabled.setChecked(lastState.interrupts().enabled(Interrupts.Interrupt.SERIAL));
        serialRequested.setChecked(lastState.interrupts().requested(Interrupts.Interrupt.SERIAL));
        joypadEnabled.setChecked(lastState.interrupts().enabled(Interrupts.Interrupt.JOYPAD));
        joypadRequested.setChecked(lastState.interrupts().requested(Interrupts.Interrupt.JOYPAD));

        registerA.setText(hex(lastState.registers().readA(), 2));
        registerB.setText(hex(lastState.registers().readB(), 2));
        registerC.setText(hex(lastState.registers().readC(), 2));
        registerD.setText(hex(lastState.registers().readD(), 2));
        registerE.setText(hex(lastState.registers().readE(), 2));
        registerF.setText(hex(lastState.registers().readF(), 2));
        registerH.setText(hex(lastState.registers().readH(), 2));
        registerL.setText(hex(lastState.registers().readL(), 2));
        registerAF.setText(hex(lastState.registers().readAF(), 4));
        registerBC.setText(hex(lastState.registers().readBC(), 4));
        registerDE.setText(hex(lastState.registers().readDE(), 4));
        registerHL.setText(hex(lastState.registers().readHL(), 4));

        gui.updateScreen();
    }

    private Component createPanel() {
        Panel mainPanel = new Panel();
        mainPanel.setLayoutManager(new GridLayout(3));

        Panel leftPanel = new Panel();
        leftPanel.setLayoutManager(new LinearLayout());
        leftPanel.addComponent(createProgramCounterPanel());
        leftPanel.addComponent(createStackPointerPanel());
        leftPanel.addComponent(createFlagsPanel());
        leftPanel.addComponent(createInterruptsPanel());

        Panel middlePanel = new Panel();
        middlePanel.setLayoutManager(new LinearLayout());
        middlePanel.addComponent(createRegistersPanel());

        Panel rightPanel = new Panel();
        rightPanel.setLayoutManager(new LinearLayout());

        mainPanel.addComponent(leftPanel);
        mainPanel.addComponent(middlePanel);
        mainPanel.addComponent(rightPanel);

        return mainPanel;
    }

    private Component createRegistersPanel() {
        Panel panel = new Panel();
        panel.setLayoutManager(new GridLayout(2));

        panel.addComponent(new Label("A"));
        panel.addComponent(registerA = readOnlyTextBox());

        panel.addComponent(new Label("B"));
        panel.addComponent(registerB = readOnlyTextBox());

        panel.addComponent(new Label("C"));
        panel.addComponent(registerC = readOnlyTextBox());

        panel.addComponent(new Label("D"));
        panel.addComponent(registerD = readOnlyTextBox());

        panel.addComponent(new Label("E"));
        panel.addComponent(registerE = readOnlyTextBox());

        panel.addComponent(new Label("F"));
        panel.addComponent(registerF = readOnlyTextBox());

        panel.addComponent(new Label("H"));
        panel.addComponent(registerH = readOnlyTextBox());

        panel.addComponent(new Label("L"));
        panel.addComponent(registerL = readOnlyTextBox());

        panel.addComponent(new Label("AF"));
        panel.addComponent(registerAF = readOnlyTextBox());

        panel.addComponent(new Label("BC"));
        panel.addComponent(registerBC = readOnlyTextBox());

        panel.addComponent(new Label("DE"));
        panel.addComponent(registerDE = readOnlyTextBox());

        panel.addComponent(new Label("HL"));
        panel.addComponent(registerHL = readOnlyTextBox());

        return panel.withBorder(Borders.singleLine("Registers"));
    }

    private Component createInterruptsPanel() {
        Panel panel = new Panel();
        panel.setLayoutManager(new GridLayout(3));

        panel.addComponent(new EmptySpace());
        panel.addComponent(new Label("Enabled"));
        panel.addComponent(new Label("Requested"));

        panel.addComponent(new Label("VBLANK"));
        panel.addComponent(vblankEnabled = readOnlyCheckBox());
        panel.addComponent(vblankRequested = readOnlyCheckBox());

        panel.addComponent(new Label("LCD"));
        panel.addComponent(lcdEnabled = readOnlyCheckBox());
        panel.addComponent(lcdRequested = readOnlyCheckBox());

        panel.addComponent(new Label("TIMER"));
        panel.addComponent(timerEnabled = readOnlyCheckBox());
        panel.addComponent(timerRequested = readOnlyCheckBox());

        panel.addComponent(new Label("SERIAL"));
        panel.addComponent(serialEnabled = readOnlyCheckBox());
        panel.addComponent(serialRequested = readOnlyCheckBox());

        panel.addComponent(new Label("JOYPAD"));
        panel.addComponent(joypadEnabled = readOnlyCheckBox());
        panel.addComponent(joypadRequested = readOnlyCheckBox());

        return panel.withBorder(Borders.singleLine("Interrupts"));
    }

    private Component createFlagsPanel() {
        Panel panel = new Panel();
        panel.setLayoutManager(new GridLayout(2));

        panel.addComponent(new Label("ZERO"));
        panel.addComponent(zeroFlag = readOnlyCheckBox());

        panel.addComponent(new Label("SUBTRACT"));
        panel.addComponent(subtractFlag = readOnlyCheckBox());

        panel.addComponent(new Label("CARRY"));
        panel.addComponent(carryFlag = readOnlyCheckBox());

        panel.addComponent(new Label("HALF CARRY"));
        panel.addComponent(halfCarryFlag = readOnlyCheckBox());

        return panel.withBorder(Borders.singleLine("Flags"));
    }

    private Component createProgramCounterPanel() {
        Panel panel = new Panel();
        panel.setLayoutManager(new GridLayout(2));

        panel.addComponent(new Label("Value"));
        panel.addComponent(programCounter = readOnlyTextBox());

        panel.addComponent(new Label("Instruction"));
        panel.addComponent(instruction = readOnlyTextBox());

        return panel.withBorder(Borders.singleLine("Program Counter"));
    }

    private Component createStackPointerPanel() {
        Panel panel = new Panel();
        panel.setLayoutManager(new GridLayout(2));

        panel.addComponent(new Label("Value"));
        panel.addComponent(stackPointer = readOnlyTextBox());

        return panel.withBorder(Borders.singleLine("Stack Pointer"));
    }

    private TextBox readOnlyTextBox() {
        TextBox textBox = new TextBox();
        textBox.setEnabled(false);
        return textBox;
    }

    private CheckBox readOnlyCheckBox() {
        CheckBox checkBox = new CheckBox();
        checkBox.setEnabled(false);
        return checkBox;
    }

    private void run() {
        try {
            Terminal terminal = new DefaultTerminalFactory().createTerminal();
            Screen screen = new TerminalScreen(terminal);
            screen.startScreen();

            BasicWindow window = new BasicWindow();
            window.setComponent(createPanel());

            MultiWindowTextGUI gui = new MultiWindowTextGUI(screen, new DefaultWindowManager(), new EmptySpace(TextColor.ANSI.BLUE));
            gui.addWindow(window);
            while (true) {
                redraw(gui);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
