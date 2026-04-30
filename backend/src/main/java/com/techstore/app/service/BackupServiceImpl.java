package com.techstore.app.service;

import com.techstore.app.exception.BackupException;
import com.techstore.app.service.interfaces.BackupService;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

@Service
public class BackupServiceImpl implements BackupService {

	private static final long TIMEOUT_MINUTES = 5L;
	private static final Pattern SAFE_TOKEN = Pattern.compile("^[A-Za-z0-9_./:@=,\\-]+$");

	private final Path scriptsDirectory = Path.of("scripts").toAbsolutePath().normalize();

	@Override
	public String execute(String command) {
		CommandSpec commandSpec = parseCommand(command);
		return runScript(commandSpec.scriptPath(), commandSpec.arguments().toArray(String[]::new));
	}

	private CommandSpec parseCommand(String command) {
		if (command == null) {
			throw new BackupException("Command is required.");
		}

		String trimmed = command.trim();
		if (trimmed.isEmpty()) {
			throw new BackupException("Command is required.");
		}

		List<String> tokens = new ArrayList<>(Arrays.asList(trimmed.split("\\s+")));
		if (tokens.isEmpty()) {
			throw new BackupException("Command is required.");
		}

		for (String token : tokens) {
			if (!SAFE_TOKEN.matcher(token).matches()) {
				throw new BackupException("Command contains invalid characters.");
			}
		}

		Path scriptPath = resolveScript(tokens.get(0));
		List<String> arguments = tokens.subList(1, tokens.size());

		return new CommandSpec(scriptPath, arguments);
	}

	private Path resolveScript(String scriptToken) {
		String normalized = scriptToken.trim();
		if (normalized.isEmpty()) {
			throw new BackupException("Script name is required.");
		}

		Path resolvedScript;
		if (normalized.startsWith("./scripts/") || normalized.startsWith("scripts/")) {
			resolvedScript = Path.of(normalized).toAbsolutePath().normalize();
		} else {
			resolvedScript = scriptsDirectory.resolve(normalized).toAbsolutePath().normalize();
		}

		if (!resolvedScript.startsWith(scriptsDirectory)) {
			throw new BackupException("Script must be inside the scripts directory.");
		}

		if (!Files.isRegularFile(resolvedScript)) {
			throw new BackupException("Script not found. Available scripts: " + String.join(", ", availableScripts()));
		}

		if (!resolvedScript.getFileName().toString().endsWith(".sh")) {
			throw new BackupException("Only .sh scripts are allowed.");
		}

		return resolvedScript;
	}

	private List<String> availableScripts() {
		try (var stream = Files.list(scriptsDirectory)) {
			return stream
					.filter(path -> Files.isRegularFile(path) && path.getFileName().toString().endsWith(".sh"))
					.map(path -> "./scripts/" + path.getFileName())
					.sorted()
					.toList();
		} catch (IOException exception) {
			throw new BackupException("Unable to read available scripts.");
		}
	}

	private String runScript(Path scriptPath, String... arguments) {
		if (!Files.exists(scriptPath)) {
			throw new BackupException("Script not found: " + scriptPath);
		}

		List<String> command = new ArrayList<>();
		command.add("bash");
		command.add(scriptPath.toString());
		command.addAll(List.of(arguments));

		ProcessBuilder processBuilder = new ProcessBuilder(command);
		processBuilder.redirectErrorStream(true);

		try {
			Process process = processBuilder.start();

			if (!process.waitFor(TIMEOUT_MINUTES, TimeUnit.MINUTES)) {
				process.destroyForcibly();
				throw new BackupException("Backup command timed out.");
			}

			String output = readProcessOutput(process);

			if (process.exitValue() != 0) {
				throw new BackupException("Backup command failed: " + output.trim());
			}

			return output.trim();
		} catch (IOException exception) {
			throw new BackupException("Failed to execute backup command.");
		} catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
			throw new BackupException("Backup command was interrupted.");
		}
	}

	private record CommandSpec(Path scriptPath, List<String> arguments) {}

	private String readProcessOutput(Process process) throws IOException {
		try (BufferedReader reader = new BufferedReader(
				new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
			return reader.lines().reduce("", (accumulator, line) -> accumulator + line + System.lineSeparator());
		}
	}
}
