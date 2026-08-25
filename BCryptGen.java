import at.favre.lib.crypto.bcrypt.BCrypt;

public class BCryptGen {
    public static void main(String[] args) {
        String hash = BCrypt.withDefaults().hashToString(10, "123456".toCharArray());
        System.out.println("HASH: " + hash);
    }
}
