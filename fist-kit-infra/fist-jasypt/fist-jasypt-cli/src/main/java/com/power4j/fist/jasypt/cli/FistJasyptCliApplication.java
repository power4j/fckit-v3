package com.power4j.fist.jasypt.cli;

import com.power4j.fist.jasypt.core.GmEncryptedValue;
import com.power4j.fist.jasypt.core.GmTextEncryptor;
import com.power4j.tile.crypto.utils.Sm3Util;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * FIST 国密配置加密命令入口。
 */
public final class FistJasyptCliApplication {

	private static final String DEFAULT_MASTER_KEY_ENV = "FIST_JASYPT_MASTER_KEY";

	private static final SecureRandom SECURE_RANDOM = new SecureRandom();

	private FistJasyptCliApplication() {
	}

	public static void main(String[] args) {
		if (args.length == 0 || isHelp(args[0])) {
			System.out.println(helpText());
			return;
		}
		String command = args[0];
		Map<String, String> options = parseOptions(args);
		if ("encrypt".equals(command)) {
			System.out.println(new GmTextEncryptor().encrypt(required(options, "--value"), resolveMasterKey(options),
					cipherPrefix(options), cipherSuffix(options)));
			return;
		}
		if ("decrypt".equals(command)) {
			System.out.println(new GmTextEncryptor().decrypt(required(options, "--value"), resolveMasterKey(options),
					cipherPrefix(options), cipherSuffix(options)));
			return;
		}
		if ("generate-key".equals(command)) {
			System.out.println(generateKey(options));
			return;
		}
		if ("fingerprint".equals(command)) {
			System.out.println(fingerprint(resolveMasterKey(options)));
			return;
		}
		throw new IllegalArgumentException("Unsupported command: " + command + ". Run `help` for usage.");
	}

	private static Map<String, String> parseOptions(String[] args) {
		Map<String, String> options = new LinkedHashMap<>();
		for (int i = 1; i < args.length; i++) {
			String key = args[i];
			if (!key.startsWith("--")) {
				throw new IllegalArgumentException("Unsupported argument: " + key);
			}
			if (i + 1 >= args.length || args[i + 1].startsWith("--")) {
				throw new IllegalArgumentException("Value is required for " + key);
			}
			options.put(key, args[++i]);
		}
		return options;
	}

	private static String required(Map<String, String> options, String key) {
		String value = options.get(key);
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException(key + " is required.");
		}
		return value;
	}

	private static boolean isHelp(String command) {
		return "help".equals(command) || "--help".equals(command) || "-h".equals(command);
	}

	private static String cipherPrefix(Map<String, String> options) {
		return options.getOrDefault("--prefix", GmEncryptedValue.PREFIX);
	}

	private static String cipherSuffix(Map<String, String> options) {
		return options.getOrDefault("--suffix", GmEncryptedValue.SUFFIX);
	}

	private static String helpText() {
		return """
				Usage:
				  fist-jasypt-cli <command> [options]

				Commands:
				  encrypt        Encrypt a plain config value.
				  decrypt        Decrypt an encrypted config value.
				  generate-key   Generate a Base64 random key.
				  fingerprint    Print the SM3 fingerprint of the master key.
				  help           Print this help message.

				Options:
				  --value <text>             Plain text for encrypt, or cipher text for decrypt.
				  --master-key-file <path>   File containing the master key.
				  --master-key-env <name>    Environment variable name for the master key. Default: FIST_JASYPT_MASTER_KEY.
				  --prefix <text>            Cipher text prefix. Default: GMENC(
				  --suffix <text>            Cipher text suffix. Default: )
				  --bytes <number>           Random key byte length for generate-key. Default: 32.
				""";
	}

	private static String resolveMasterKey(Map<String, String> options) {
		String masterKeyFile = options.get("--master-key-file");
		if (masterKeyFile != null && !masterKeyFile.isBlank()) {
			try {
				String value = Files.readString(Path.of(masterKeyFile), StandardCharsets.UTF_8).trim();
				if (!value.isBlank()) {
					return value;
				}
			}
			catch (Exception ex) {
				throw new IllegalArgumentException("--master-key-file cannot be read.", ex);
			}
			throw new IllegalArgumentException("--master-key-file is empty.");
		}
		String envName = options.getOrDefault("--master-key-env", DEFAULT_MASTER_KEY_ENV);
		String envValue = System.getenv(envName);
		if (envValue != null && !envValue.isBlank()) {
			return envValue.trim();
		}
		throw new IllegalArgumentException("--master-key-file is required when " + envName + " is not set.");
	}

	private static String generateKey(Map<String, String> options) {
		int bytes = Integer.parseInt(options.getOrDefault("--bytes", "32"));
		if (bytes < 16) {
			throw new IllegalArgumentException("--bytes must be greater than or equal to 16.");
		}
		byte[] key = new byte[bytes];
		SECURE_RANDOM.nextBytes(key);
		return Base64.getEncoder().encodeToString(key);
	}

	private static String fingerprint(String masterKey) {
		byte[] hash = Sm3Util.hash(masterKey.getBytes(StandardCharsets.UTF_8), 16, null);
		return "SM3:" + Base64.getEncoder().encodeToString(hash);
	}

}
