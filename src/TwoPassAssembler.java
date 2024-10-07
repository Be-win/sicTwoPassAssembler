import java.io.*;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class TwoPassAssembler {
    private final String inputFile;
    private final Map<String, String> optab = new HashMap<>();
    private final Map<String, Integer> symtab = new LinkedHashMap<>();
    private final Map<Integer, String> intermediate = new LinkedHashMap<>();
    private final Map<Integer, String> intermediateStart = new LinkedHashMap<>();
    private final Map<Integer, String> objectCode = new LinkedHashMap<>();
    private int locctr = 0;
    private int start = 0;
    private int length = 0;

    public TwoPassAssembler(String inputFile) {
        this.inputFile = inputFile;
    }

    public void loadOptab() throws IOException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("optab.txt");
        if (inputStream == null) {
            throw new FileNotFoundException("Resource not found: optab.txt");
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] parts = line.split("\\s+");
            optab.put(parts[0], parts[1]);
        }
        reader.close();
    }

    public void passOne() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(inputFile));
        String line = reader.readLine();
        String[] parts = line.split("\\s+");

        if (parts[1].equals("START")) {
            start = Integer.parseInt(parts[2], 16); // Parse start as hexadecimal
            locctr = start;
            intermediateStart.put(locctr, String.format("\t%s\t%s\t%s", parts[0], parts[1], parts[2]));
            line = reader.readLine();
        } else {
            locctr = 0;
        }

        // Process each line
        while (line != null) {
            parts = line.split("\\s+");
            if (parts[1].equals("END")) break;

            // Store intermediate with location counter in hex format
            intermediate.put(locctr, String.format("\t%s\t%s\t%s", parts[0], parts[1], parts[2]));

            // Insert into symbol table if label exists
            if (!parts[0].equals("-")) {
                symtab.put(parts[0], locctr);
            }

            // Update locctr based on the opcode
            if (optab.containsKey(parts[1])) {
                locctr += 3;
            } else if (parts[1].equals("WORD")) {
                locctr += 3;
            } else if (parts[1].equals("BYTE")) {
                locctr += parts[2].length() - 3;
            } else if (parts[1].equals("RESW")) {
                locctr += 3 * Integer.parseInt(parts[2]);
            } else if (parts[1].equals("RESB")) {
                locctr += Integer.parseInt(parts[2]);
            }

            line = reader.readLine();
        }

        // Store final line in intermediate and calculate program length
        intermediate.put(locctr, String.format("\t%s\t%s\t%s", parts[0], parts[1], parts[2]));
        length = locctr - start;

        reader.close();
    }

    public void passTwo(){
        String line;
        String[] parts;

        String startLine = intermediateStart.get(start);
        String[] startParts = startLine.trim().split("\\s+");

        // Handle "START" directive
        if (startParts[1].equals("START")) {
            objectCode.put(0, "H^ " + startParts[0] + "^ " + String.format("%06X", start) + "^ " + String.format("%06X", length));
            //line = intermediate.get(start + 1);
        } else {
            objectCode.put(0, "H^ " + " " + "^ 0000^ " + String.format("%06X", length));
        }

        StringBuilder textRecord = new StringBuilder();
        int textStartAddr = 0;
        int textLength = 0;

        for (int loc : intermediate.keySet()) {

            line = intermediate.get(loc);
            parts = line.trim().split("\\s+");
            if (parts.length < 3) continue;

            if (parts[2].equals("END")) break;

            if (textLength == 0) {
                textStartAddr = loc;
                textRecord.append("T^ ").append(String.format("%06X", textStartAddr)).append("^ ");
            }

            // Generate object code for each line
            if (optab.containsKey(parts[1])) {
                String machineCode = optab.get(parts[1]);
                int address = symtab.getOrDefault(parts[2], 0);
                String code = machineCode + String.format("%04X", address);
                textRecord.append(code).append("^ ");
                textLength += code.length() / 2;
            } else if (parts[1].equals("WORD")) {
                String wordCode = String.format("%06X", Integer.parseInt(parts[2]));
                textRecord.append(wordCode).append("^ ");
                textLength += wordCode.length() / 2;
            } else if (parts[1].equals("BYTE")) {
                String byteCode = parts[2].substring(2, parts[2].length() - 1); // Extract value from BYTE literal
                textRecord.append(byteCode).append("^ ");
                textLength += byteCode.length() / 2;
            } else if (parts[1].equals("RESW") || parts[1].equals("RESB")) {
                // If we hit RESW/RESB, flush the current text record and start a new one after reserving memory
                if (textLength > 0) {
                    objectCode.put(textStartAddr, textRecord.toString());
                    textRecord = new StringBuilder();
                    textLength = 0;
                }
                continue; // Do not generate object code for reserved space
            }

            if (textLength >= 30) { // Text records should not exceed 30 bytes (60 hex characters)
                objectCode.put(textStartAddr, textRecord.toString());
                textRecord = new StringBuilder();
                textLength = 0;
            }
        }

        // Write remaining text record if not empty
        if (textLength > 0) {
            objectCode.put(textStartAddr, textRecord.toString());
        }

        // Write End record
        objectCode.put(locctr, "E^ " + String.format("%06X", start));
    }


    public Map<Integer, String> getIntermediate() {
        return intermediate;
    }

    public Map<Integer, String> getIntermediateStart() {
        return intermediateStart;
    }

    public Map<String, Integer> getSymtab() {
        return symtab;
    }

    public Map<Integer, String> getObjectCode() {
        return objectCode;
    }

}
