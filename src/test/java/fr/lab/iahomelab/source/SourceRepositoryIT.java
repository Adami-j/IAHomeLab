package fr.lab.iahomelab.source;

import fr.lab.iahomelab.config.PostgresTestConfiguration;
import fr.lab.iahomelab.source.entity.Source;
import fr.lab.iahomelab.source.entity.SourceStatus;
import fr.lab.iahomelab.source.entity.SourceType;
import fr.lab.iahomelab.source.repository.SourceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Import(PostgresTestConfiguration.class)
class SourceRepositoryIT {

    @Autowired
    private SourceRepository sourceRepository;

    @BeforeEach
    void setUp() {
        sourceRepository.deleteAll();
    }

    @Test
    void shouldSaveSourceWithUrl() {
        Source source = new Source();
        source.setTitle("Attention Is All You Need");
        source.setUrl("https://arxiv.org/abs/1706.03762");
        source.setType(SourceType.PAPER);

        Source saved = sourceRepository.saveAndFlush(source);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getTitle()).isEqualTo("Attention Is All You Need");
        assertThat(saved.getUrl()).isEqualTo("https://arxiv.org/abs/1706.03762");
        assertThat(saved.getType()).isEqualTo(SourceType.PAPER);
        assertThat(saved.getStatus()).isEqualTo(SourceStatus.TO_READ);
    }

    @Test
    void shouldSaveSourceWithStoragePathOnly() {
        Source source = new Source();
        source.setTitle("Local paper");
        source.setStoragePath("sources/papers/local-paper.pdf");
        source.setFileName("local-paper.pdf");
        source.setMimeType("application/pdf");
        source.setType(SourceType.PAPER);

        Source saved = sourceRepository.saveAndFlush(source);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getUrl()).isNull();
        assertThat(saved.getStoragePath())
                .isEqualTo("sources/papers/local-paper.pdf");
    }

    @Test
    void shouldRejectSourceWithoutUrlAndStoragePath() {
        Source source = new Source();
        source.setTitle("Invalid source");
        source.setType(SourceType.ARTICLE);

        assertThatThrownBy(() -> sourceRepository.saveAndFlush(source))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void shouldRejectDuplicateUrl() {
        Source first = new Source();
        first.setTitle("First");
        first.setUrl("https://example.com/article");
        first.setType(SourceType.ARTICLE);

        sourceRepository.saveAndFlush(first);

        Source second = new Source();
        second.setTitle("Second");
        second.setUrl("https://example.com/article");
        second.setType(SourceType.ARTICLE);

        assertThatThrownBy(() -> sourceRepository.saveAndFlush(second))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void shouldFindSourceByUrl() {
        Source source = new Source();
        source.setTitle("Spring AI");
        source.setUrl("https://spring.io/projects/spring-ai");
        source.setType(SourceType.DOCUMENTATION);

        sourceRepository.saveAndFlush(source);

        var found = sourceRepository.findByUrl(
                "https://spring.io/projects/spring-ai"
        );

        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("Spring AI");
    }
}