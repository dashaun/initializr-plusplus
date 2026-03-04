package dev.dashaun.shell.initializr.plusplus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

final class MiseTomlEditor {

	private MiseTomlEditor() {
	}

	static String upsertAssignment(String content, String sectionName, String key, String value) {
		List<String> lines = new ArrayList<>(Arrays.asList(normalize(content).split("\n", -1)));
		String header = "[" + sectionName + "]";
		int sectionStart = findSectionStart(lines, header);
		List<String> assignmentLines = renderAssignment(key, value);

		if (sectionStart < 0) {
			appendSection(lines, header, assignmentLines);
			return join(lines);
		}

		int sectionEnd = findSectionEnd(lines, sectionStart);
		int[] existingRange = findAssignmentRange(lines, sectionStart + 1, sectionEnd, key);
		if (existingRange != null) {
			lines.subList(existingRange[0], existingRange[1]).clear();
			lines.addAll(existingRange[0], assignmentLines);
		}
		else {
			int insertAt = sectionEnd;
			while (insertAt > sectionStart + 1 && lines.get(insertAt - 1).isBlank()) {
				insertAt--;
			}
			lines.addAll(insertAt, assignmentLines);
		}
		return join(lines);
	}

	private static String normalize(String content) {
		return content.replace("\r\n", "\n");
	}

	private static int findSectionStart(List<String> lines, String header) {
		for (int i = 0; i < lines.size(); i++) {
			if (lines.get(i).trim().equals(header)) {
				return i;
			}
		}
		return -1;
	}

	private static int findSectionEnd(List<String> lines, int sectionStart) {
		for (int i = sectionStart + 1; i < lines.size(); i++) {
			String trimmed = lines.get(i).trim();
			if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
				return i;
			}
		}
		return lines.size();
	}

	private static int[] findAssignmentRange(List<String> lines, int start, int end, String key) {
		for (int i = start; i < end; i++) {
			String trimmed = lines.get(i).trim();
			if (trimmed.isEmpty() || trimmed.startsWith("#") || !trimmed.contains("=")) {
				continue;
			}
			String lineKey = parseKey(trimmed);
			if (!key.equals(lineKey)) {
				continue;
			}
			int blockEnd = i + 1;
			if (isMultilineStringStart(trimmed)) {
				while (blockEnd < end && !lines.get(blockEnd).trim().equals("'''")) {
					blockEnd++;
				}
				if (blockEnd < end) {
					blockEnd++;
				}
			}
			return new int[] { i, blockEnd };
		}
		return null;
	}

	private static String parseKey(String line) {
		String raw = line.substring(0, line.indexOf('=')).trim();
		if (raw.startsWith("\"") && raw.endsWith("\"") && raw.length() >= 2) {
			return raw.substring(1, raw.length() - 1);
		}
		return raw;
	}

	private static boolean isMultilineStringStart(String line) {
		int delimiterIndex = line.indexOf("'''");
		if (delimiterIndex < 0) {
			return false;
		}
		return line.indexOf("'''", delimiterIndex + 3) < 0;
	}

	private static List<String> renderAssignment(String key, String value) {
		String renderedKey = renderKey(key);
		if (!value.contains("\n")) {
			return List.of(renderedKey + " = " + value);
		}
		String[] pieces = value.split("\n", -1);
		List<String> lines = new ArrayList<>();
		lines.add(renderedKey + " = " + pieces[0]);
		lines.addAll(List.of(pieces).subList(1, pieces.length));
		return lines;
	}

	private static String renderKey(String key) {
		return key.matches("[A-Za-z0-9_-]+") ? key : "\"" + key + "\"";
	}

	private static void appendSection(List<String> lines, String header, List<String> assignmentLines) {
		if (!(lines.size() == 1 && lines.getFirst().isEmpty())) {
			while (!lines.isEmpty() && lines.getLast().isBlank()) {
				lines.removeLast();
			}
			if (!lines.isEmpty()) {
				lines.add("");
			}
		}
		lines.add(header);
		lines.addAll(assignmentLines);
		lines.add("");
	}

	private static String join(List<String> lines) {
		return String.join("\n", lines).stripTrailing() + "\n";
	}

}
