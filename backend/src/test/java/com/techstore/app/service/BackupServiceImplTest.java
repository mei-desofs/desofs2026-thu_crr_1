package com.techstore.app.service;

import com.techstore.app.exception.BackupException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;

import static org.junit.jupiter.api.Assertions.*;

class BackupServiceImplTest {

	private BackupServiceImpl backupService;

	@TempDir
	Path tempDir;

	private Path scriptsDirectory;

	@BeforeEach
	void setUp() {
		scriptsDirectory = tempDir.resolve("scripts");
		// Inject the temp scripts directory directly — no user.dir hacks needed.
		backupService = new BackupServiceImpl(scriptsDirectory);
	}

	private Path createScript(String name, String content) throws IOException {
		Files.createDirectories(scriptsDirectory);

		Path script = scriptsDirectory.resolve(name);
		Files.writeString(script, content);

		try {
			Files.setPosixFilePermissions(
					script,
					PosixFilePermissions.fromString("rwxr-xr-x")
			);
		} catch (UnsupportedOperationException ignored) {
			script.toFile().setExecutable(true);
		}

		return script;
	}

	// ---------------- INVALID INPUT TESTS ----------------

	@Test
	public void testExecuteWithNullCommand() {
		assertThrows(BackupException.class, () -> backupService.execute(null));
	}

	@Test
	public void testExecuteWithEmptyCommand() {
		assertThrows(BackupException.class, () -> backupService.execute(""));
	}

	@Test
	public void testExecuteWithWhitespaceCommand() {
		assertThrows(BackupException.class, () -> backupService.execute("   "));
	}

	@Test
	public void testExecuteWithInvalidCharacters() {
		BackupException ex = assertThrows(BackupException.class,
				() -> backupService.execute("backup_products.sh; rm -rf /"));

		assertTrue(ex.getMessage().contains("invalid characters"));
	}

	@Test
	public void testExecuteWithShellInjection() {
		BackupException ex = assertThrows(BackupException.class,
				() -> backupService.execute("backup_products.sh $(whoami)"));

		assertTrue(ex.getMessage().contains("invalid characters"));
	}

	@Test
	public void testExecuteWithPipeCharacter() {
		BackupException ex = assertThrows(BackupException.class,
				() -> backupService.execute("backup_products.sh | tee output.txt"));

		assertTrue(ex.getMessage().contains("invalid characters"));
	}

	@Test
	public void testExecuteWithCommandSubstitution() {
		BackupException ex = assertThrows(BackupException.class,
				() -> backupService.execute("backup_products.sh `date`"));

		assertTrue(ex.getMessage().contains("invalid characters"));
	}

	@Test
	public void testExecuteWithAmpersandRedirection() {
		BackupException ex = assertThrows(BackupException.class,
				() -> backupService.execute("backup_products.sh & background"));

		assertTrue(ex.getMessage().contains("invalid characters"));
	}

	@Test
	public void testExecuteWithInputRedirection() {
		BackupException ex = assertThrows(BackupException.class,
				() -> backupService.execute("backup_products.sh < input.txt"));

		assertTrue(ex.getMessage().contains("invalid characters"));
	}

	@Test
	public void testExecuteWithOutputRedirection() {
		BackupException ex = assertThrows(BackupException.class,
				() -> backupService.execute("backup_products.sh > output.txt"));

		assertTrue(ex.getMessage().contains("invalid characters"));
	}

	@Test
	public void testCommandWithCurlyBraces() {
		BackupException ex = assertThrows(BackupException.class,
				() -> backupService.execute("backup_products.sh {1..100}"));

		assertTrue(ex.getMessage().contains("invalid characters"));
	}

	@Test
	public void testCommandWithSquareBrackets() {
		BackupException ex = assertThrows(BackupException.class,
				() -> backupService.execute("backup_products.sh [1-100]"));

		assertTrue(ex.getMessage().contains("invalid characters"));
	}

	@Test
	public void testCommandWithGlobPattern() {
		BackupException ex = assertThrows(BackupException.class,
				() -> backupService.execute("backup_products.sh *.txt"));

		assertTrue(ex.getMessage().contains("invalid characters"));
	}

	@Test
	public void testExecuteWithPathTraversal() {
		BackupException ex = assertThrows(BackupException.class,
				() -> backupService.execute("../../../etc/passwd"));

		assertTrue(ex.getMessage().contains("Script must be inside the scripts directory"));
	}

	@Test
	public void testPathTraversalWithMultipleLevels() {
		BackupException ex = assertThrows(BackupException.class,
				() -> backupService.execute("../../../../../../etc/passwd"));

		assertTrue(ex.getMessage().contains("Script must be inside the scripts directory"));
	}

	@Test
	public void testAbsolutePathAttempt() {
		String path = System.getProperty("os.name").toLowerCase().contains("win")
				? "C:\\Windows\\System32\\drivers\\etc\\hosts"
				: "/etc/passwd";

		BackupException ex = assertThrows(BackupException.class,
				() -> backupService.execute(path));

		assertTrue(
				ex.getMessage().contains("Script must be inside the scripts directory")
						|| ex.getMessage().contains("Script not found")
		);
	}

	@Test
	public void testExecuteWithWhitespaceInCommand() {
		BackupException ex = assertThrows(BackupException.class,
				() -> backupService.execute("backup products.sh"));

		assertNotNull(ex.getMessage());
	}

	// ---------------- VALID EXECUTION TESTS ----------------

	@Test
	@DisabledOnOs(OS.WINDOWS)
	public void testExecuteValidScriptSuccessfully() throws IOException {
		createScript("backup_products.sh", "#!/bin/bash\necho \"Backup completed\"");

		String result = backupService.execute("backup_products.sh");

		assertEquals("Backup completed", result);
	}

	@Test
	@DisabledOnOs(OS.WINDOWS)
	public void testExecuteValidScriptWithArguments() throws IOException {
		createScript("backup_products.sh", "#!/bin/bash\necho \"$1 $2\"");

		String result = backupService.execute("backup_products.sh arg1 arg2");

		assertEquals("arg1 arg2", result);
	}

	@Test
	@DisabledOnOs(OS.WINDOWS)
	public void testExecuteScriptWithNonZeroExit() throws IOException {
		createScript("failing_script.sh", "#!/bin/bash\necho \"failure\"\nexit 1");

		BackupException ex = assertThrows(BackupException.class,
				() -> backupService.execute("failing_script.sh"));

		assertTrue(ex.getMessage().contains("Backup command failed"));
	}
}