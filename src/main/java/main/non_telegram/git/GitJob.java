package main.non_telegram.git;


import lombok.extern.slf4j.Slf4j;
import main.Main;
import main.common.QuartzJobsList;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.jooq.lambda.tuple.Tuple2;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Stream;

import static java.nio.file.Files.*;
import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.time.LocalDateTime.now;
import static java.util.concurrent.TimeUnit.SECONDS;
import static main.common.QuartzUtils.buildJob;

@Slf4j
@Component
@DisallowConcurrentExecution
public class GitJob implements Job, QuartzJobsList {
	@Value("${app.git.login}")
	private String login;
	@Value("${app.git.password}")
	private String pass;
	@Value("${app.git.url}")
	private String gitUrl;
	private final String gitLocalPath = "/tmp/" + UUID.randomUUID();

	@PostConstruct
	private void init() throws IOException {
		createDirectory(Path.of(gitLocalPath));
	}

	@Override
	public List<Tuple2<JobDetail, Trigger>> getJobs() {
		return List.of(
				buildJob(GitJob.class, "0 0 4 * * ?")
		);
	}

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		try {
			execute();
		} catch (GitAPIException | IOException | NoSuchElementException | InterruptedException e) {
			log.error("Error git job", e);
		}
	}

	private void execute() throws GitAPIException, IOException, InterruptedException {
		CredentialsProvider cred = creds();

		Path repoLocalPath = Path.of(gitLocalPath);
		clearDir(repoLocalPath);

		CloneCommand cloneCommand = Git.cloneRepository()
				.setURI(gitUrl)
				.setDirectory(repoLocalPath.toFile())
				.setCredentialsProvider(cred);

		int count = new SecureRandom().nextInt(35) + 3;

		try (Git git = cloneCommand.call()) {
			Path gitDir = git.getRepository().getWorkTree().toPath();
			try (Stream<Path> textFile = walk(gitDir).filter(this::filter)) {
				Path file = textFile.findFirst().orElseThrow();
				for (int i = 0; i < count; i++) {
					SECONDS.sleep(1);
					doChanges(file, git);
				}
			}

			git.push().setCredentialsProvider(cred).call();
		}
	}

	private void doChanges(Path file, Git git) throws IOException, GitAPIException {
		smartChangeFile(file);

		String message = "add again at " + now(Main.zone);
		git.commit()
				.setMessage(message)
				.setAuthor("", login)
				.setAll(true)
				.call();
	}

	private void smartChangeFile(Path file) throws IOException {
		int size = readAllLines(file).size();
		var option = size > 1000 ? TRUNCATE_EXISTING : APPEND;

		writeString(file, "\ntext", option);
	}

	private boolean filter(Path f) {
		return f.getFileName().toString().equals("text.txt");
	}

	private CredentialsProvider creds() {
		return new UsernamePasswordCredentialsProvider(login, pass);
	}

	private void clearDir(Path dir) {
		try (Stream<Path> walk = walk(dir)) {
			walk.sorted(Comparator.reverseOrder())
					.filter(path -> !dir.equals(path))
					.map(Path::toFile)
					.forEach(File::delete);
		} catch (IOException e) {
			log.error("Git error clean dir", e);
			throw new RuntimeException(e);
		}
	}
}
