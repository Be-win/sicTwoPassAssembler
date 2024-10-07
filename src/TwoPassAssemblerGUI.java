import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.InputStream;
import java.util.Map;
import java.io.IOException;

public class TwoPassAssemblerGUI extends JFrame {
    private final JTextField inputFileField;
    private JTextArea intermediateFileOutput, symbolTableOutput, objectCodeOutput;
    private JPanel mainPanel2;
    private final JPanel cardPanel;
    private final CardLayout cardLayout;

    public TwoPassAssemblerGUI() throws IOException {
        setTitle("Two Pass Assembler");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null); // Center the window

        // Set background color
        Color backgroundColor = new Color(204, 204, 204); // Light gray color

        Font font = new Font("Arial", Font.PLAIN, 16);
        Font font2 = new Font("Monospaced", Font.PLAIN, 14);

        // Load the image
        Icon imageIcon = getBrowseIcon("java.png");
        JLabel imageLabel = new JLabel(imageIcon);
        imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT); // Center alignment for the image

        // Input label
        JLabel inputFileLabel = new JLabel("Input File:");
        inputFileLabel.setFont(font);
        inputFileLabel.setPreferredSize(new Dimension(100, 30));
        inputFileLabel.setAlignmentX(Component.CENTER_ALIGNMENT); // Center alignment

        // Input field
        inputFileField = new JTextField(30);
        inputFileField.setFont(font);
        inputFileField.setPreferredSize(new Dimension(250, 25));
        inputFileField.setAlignmentX(Component.CENTER_ALIGNMENT); // Center alignment

        // Browse button
        JButton browseBtn = new JButton("Browse");
        browseBtn.setFont(font);
        Icon browseIcon = getBrowseIcon("browse-icon.png");
        browseBtn.setIcon(browseIcon);
        browseBtn.setAlignmentX(Component.CENTER_ALIGNMENT); // Center alignment
        browseBtn.addActionListener(e -> browseFile(inputFileField));

        // Input panel for the text field and button
        JPanel inputFieldsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        inputFieldsPanel.setBackground(backgroundColor);
        inputFieldsPanel.add(inputFileLabel);
        inputFieldsPanel.add(inputFileField);
        inputFieldsPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        inputFieldsPanel.add(browseBtn);

        // Assemble button with styling
        JButton assembleBtn = new JButton("Assemble");
        assembleBtn.setFont(new Font("Arial", Font.BOLD, 16));
        assembleBtn.setBackground(new Color(0, 153, 255));
        assembleBtn.setForeground(Color.WHITE);
        assembleBtn.setFocusPainted(false);
        assembleBtn.setPreferredSize(new Dimension(120, 35));
        assembleBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Panel to hold the assemble button, centered
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(backgroundColor);
        buttonPanel.add(assembleBtn);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));

        // Heading label
        JLabel headingLabel = new JLabel("Two Pass Assembler", JLabel.CENTER);
        headingLabel.setFont(new Font("Arial", Font.BOLD, 24));
        headingLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        headingLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Create a main panel to hold the image, heading, input file panel, and the button
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(backgroundColor); // Set main panel background
        mainPanel.add(Box.createVerticalGlue());
        mainPanel.add(imageLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(headingLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(inputFieldsPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(buttonPanel);
        mainPanel.add(Box.createVerticalGlue());

        // Create the second state for the GUI (after pressing Assemble)
        createSecondPanel(font2, backgroundColor);

        // Create card layout to switch between panels
        cardPanel = new JPanel(new CardLayout());
        cardPanel.add(mainPanel, "mainPanel");
        cardPanel.add(mainPanel2, "secondPanel");

        cardLayout = (CardLayout) cardPanel.getLayout();
        add(cardPanel);

        // Action listener for the Assemble button to run the assembler
        assembleBtn.addActionListener(e -> {
            String inputFilePath = inputFileField.getText().trim();
            File inputFile = new File(inputFilePath);

            if (!inputFile.exists() || !inputFile.isFile()) {
                JOptionPane.showMessageDialog(TwoPassAssemblerGUI.this, "Invalid input file path.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                TwoPassAssembler assembler = new TwoPassAssembler(inputFilePath);
                assembler.loadOptab(); // Load opcode table
                assembler.passOne(); // First pass
                assembler.passTwo(); // Second pass

                // Display results
                displayResults(assembler);

                // Show the second panel
                cardLayout.show(cardPanel, "secondPanel");
                setLocationRelativeTo(null); // Recenter the window
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(TwoPassAssemblerGUI.this, "Error during assembly: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    //Browse file
    private void browseFile(JTextField field) {
        JFileChooser fileChooser = new JFileChooser();
        int option = fileChooser.showOpenDialog(this);
        if (option == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            field.setText(file.getPath());
        }
    }

    private void displayResults(TwoPassAssembler assembler) {
        // Display the intermediate file output
        StringBuilder intermediateOutput = new StringBuilder();
        for (Map.Entry<Integer, String> entry : assembler.getIntermediateStart().entrySet()) {
            intermediateOutput.append(String.format("%04X\t%s%n", entry.getKey(), entry.getValue()));
        }
        for (Map.Entry<Integer, String> entry : assembler.getIntermediate().entrySet()) {
            intermediateOutput.append(String.format("%04X\t%s%n", entry.getKey(), entry.getValue()));
        }
        intermediateFileOutput.setText(intermediateOutput.toString());

        // Display the symbol table output
        StringBuilder symbolTableOutputStr = new StringBuilder();
        for (Map.Entry<String, Integer> entry : assembler.getSymtab().entrySet()) {
            symbolTableOutputStr.append(String.format("%s\t%04X%n", entry.getKey(), entry.getValue()));
        }
        symbolTableOutput.setText(symbolTableOutputStr.toString());

        // Display the object code output
        StringBuilder objectCodeOutputStr = new StringBuilder();
        for (Map.Entry<Integer, String> entry : assembler.getObjectCode().entrySet()) {
            objectCodeOutputStr.append(entry.getValue()).append("\n");
        }
        objectCodeOutput.setText(objectCodeOutputStr.toString());
    }

    private void createSecondPanel(Font font2, Color backgroundColor) {
        intermediateFileOutput = new JTextArea(10, 30);
        intermediateFileOutput.setBorder(BorderFactory.createTitledBorder("Intermediate File Output"));
        intermediateFileOutput.setFont(font2);

        symbolTableOutput = new JTextArea(10, 30);
        symbolTableOutput.setBorder(BorderFactory.createTitledBorder("Symbol Table Output"));
        symbolTableOutput.setFont(font2);

        JPanel textAreaPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        textAreaPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        textAreaPanel.add(new JScrollPane(intermediateFileOutput));
        textAreaPanel.add(new JScrollPane(symbolTableOutput));

        objectCodeOutput = new JTextArea(9, 91);
        objectCodeOutput.setBorder(BorderFactory.createTitledBorder("Object Code Output"));
        objectCodeOutput.setFont(font2);

        JPanel bottomTextAreaPanel = new JPanel();
        bottomTextAreaPanel.add(new JScrollPane(objectCodeOutput));

        final var returnBtn = getjButton();

        JPanel returnButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        returnButtonPanel.add(returnBtn);
        returnButtonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0));

        mainPanel2 = new JPanel();
        mainPanel2.setLayout(new BoxLayout(mainPanel2, BoxLayout.Y_AXIS));
        mainPanel2.setBackground(backgroundColor); // Set secondary panel background
        textAreaPanel.setBackground(backgroundColor);
        bottomTextAreaPanel.setBackground(backgroundColor);
        returnButtonPanel.setBackground(backgroundColor);
        mainPanel2.add(textAreaPanel);
        mainPanel2.add(bottomTextAreaPanel);
        mainPanel2.add(returnButtonPanel);
    }

    private JButton getjButton() {
        JButton returnBtn = new JButton("Return");
        returnBtn.setFont(new Font("Arial", Font.BOLD, 16)); // Slightly reduce the font size
        returnBtn.setBackground(new Color(0, 153, 255));
        returnBtn.setForeground(Color.WHITE); // White text
        returnBtn.setFocusPainted(false); // Remove focus border
        returnBtn.setPreferredSize(new Dimension(120, 35));

        returnBtn.addActionListener(e -> {
            cardLayout.show(cardPanel, "mainPanel");
            setLocationRelativeTo(null);
        });
        return returnBtn;
    }

    public Icon getBrowseIcon(String path) throws IOException {
        // Use ClassLoader to get the resource as an InputStream
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(path);
        if (inputStream == null) {
            System.out.println("Resource not found: " + path);
            return null;
        }

        // Create an ImageIcon from the InputStream
        return new ImageIcon(inputStream.readAllBytes());
    }
}
