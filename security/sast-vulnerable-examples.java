//  уязвимые примеры для Semgrep
class SastVulnerableExamples {
    void command(String userInput) throws Exception {
        Runtime.getRuntime().exec(userInput);
    }

    void sql(org.springframework.jdbc.core.JdbcTemplate jdbc, String id) {
        jdbc.query("select * from products where id = '" + id, (rs, row) -> rs.getString(1));
    }

    void upload(org.springframework.web.multipart.MultipartFile file, java.nio.file.Path path) throws Exception {
        String name = file.getOriginalFilename();
        file.transferTo(path.resolve(name));
    }

    void password() {
        String password = "admin123";
    }
}
