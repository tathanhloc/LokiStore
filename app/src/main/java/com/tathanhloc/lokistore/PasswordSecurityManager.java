package com.tathanhloc.lokistore;

import android.util.Base64;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class PasswordSecurityManager {
    private static final int ITERATION_COUNT = 65536;  // Số lần lặp mạnh mẽ
    private static final int KEY_LENGTH = 256;         // Độ dài khóa
    private static final int SALT_LENGTH = 16;         // Độ dài salt

    /**
     * Mã hóa mật khẩu với salt và nhiều lần lặp
     * @param password Mật khẩu gốc
     * @return Chuỗi mã hóa an toàn
     */
    public static String hashPassword(String password) {
        try {
            // Tạo salt ngẫu nhiên
            byte[] salt = generateSalt();

            // Tạo khóa từ mật khẩu
            KeySpec spec = new PBEKeySpec(
                    password.toCharArray(),
                    salt,
                    ITERATION_COUNT,
                    KEY_LENGTH
            );

            // Sử dụng thuật toán mã hóa mạnh
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256"); // PBKDF2 với SHA-256
            byte[] hash = factory.generateSecret(spec).getEncoded();// Tạo hash

            // Kết hợp salt và hash
            byte[] combined = new byte[salt.length + hash.length];
            System.arraycopy(salt, 0, combined, 0, salt.length);
            System.arraycopy(hash, 0, combined, salt.length, hash.length);

            // Mã hóa base64 để lưu trữ
            return Base64.encodeToString(combined, Base64.DEFAULT);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi mã hóa mật khẩu", e);
        }
    }

    /**
     * Xác thực mật khẩu
     * @param inputPassword Mật khẩu nhập vào
     * @param storedHash Mật khẩu đã mã hóa được lưu trữ
     * @return Kết quả xác thực
     */
    public static boolean verifyPassword(String inputPassword, String storedHash) {
        try {
            // Giải mã base64
            byte[] combined = Base64.decode(storedHash, Base64.DEFAULT);

            // Tách salt và hash
            byte[] salt = new byte[SALT_LENGTH];
            byte[] originalHash = new byte[combined.length - SALT_LENGTH];

            System.arraycopy(combined, 0, salt, 0, SALT_LENGTH);
            System.arraycopy(combined, SALT_LENGTH, originalHash, 0, originalHash.length);

            // Tạo hash mới từ mật khẩu nhập vào
            KeySpec spec = new PBEKeySpec(
                    inputPassword.toCharArray(),
                    salt,
                    ITERATION_COUNT,
                    KEY_LENGTH
            );

            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] newHash = factory.generateSecret(spec).getEncoded();

            // So sánh hash
            return MessageDigest.isEqual(originalHash, newHash);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Sinh salt ngẫu nhiên
     * @return Mảng byte salt
     */
    private static byte[] generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_LENGTH];
        random.nextBytes(salt);
        return salt;
    }

    /**
     * Kiểm tra độ mạnh mật khẩu
     * @param password Mật khẩu cần kiểm tra
     * @return Kết quả đánh giá
     */
    public static boolean isPasswordStrong(String password) {
        // Kiểm tra các tiêu chí độ mạnh mật khẩu
        return password != null &&
                password.length() >= 8 &&           // Ít nhất 8 ký tự
                password.matches(".*[A-Z].*") &&    // Có chữ hoa
                password.matches(".*[a-z].*") &&    // Có chữ thường
                password.matches(".*\\d.*") &&      // Có số
                password.matches(".*[!@#$%^&*()].*"); // Có ký tự đặc biệt
    }
}