package filteringgasstations.database.models;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

/**
 * #AndreasReview
 */
@Entity(name = "input_files")
@Table
public class InputFile {

    private String id;
    private String hashsum;

    public InputFile() {
    }

    public InputFile(String id, String hashsum) {
        this.id = id;
        this.hashsum = hashsum;
    }

    @Id
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getHashsum() {
        return hashsum;
    }

    public void setHashsum(String hashsum) {
        this.hashsum = hashsum;
    }


    public static Optional<String> getChecksum(String filename) {
        try {
            var file = ClassLoader.getSystemClassLoader().getResource(filename);
            assert file != null;
            String content = Files.asCharSource(new File(file.getPath()), Charsets.UTF_8).read();
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(content.getBytes(), 0, content.length());
            String signature = new BigInteger(1, md5.digest()).toString(16);
            return Optional.of(signature);
        } catch (final NoSuchAlgorithmException | IOException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }
}
