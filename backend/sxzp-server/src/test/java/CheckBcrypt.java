import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class CheckBcrypt {
    public static void main(String[] args) {
        String hash = args[0];
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        System.out.println("matches(123456) = " + encoder.matches("123456", hash));
        System.out.println("matches(admin)  = " + encoder.matches("admin", hash));
        // 顺便生成一个确定对应 123456 的新 hash 备用
        System.out.println("NEW_HASH_123456 = " + encoder.encode("123456"));
    }
}
