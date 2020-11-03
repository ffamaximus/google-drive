/**
 * 
 */
package co.com.alianza.drive.controllers;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

/**
 * @author Andres
 *
 */
@RestController
@RequestMapping("archivos")
public class ArchivosController {

	private static final String APPLICATION_NAME = "Google Drive API Java Quickstart";
	private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
	private static final String TOKENS_DIRECTORY_PATH = "tokens";

	@Value("${co.com.alianza.drive.credenciales-google}")
	private Resource credenciales;

	private static Resource CREDENCIALES;

	@Value("${co.com.alianza.drive.credenciales-google}")
	public void setNameStatic(Resource name) {
		ArchivosController.CREDENCIALES = name;
	}

	@GetMapping()
	public ResponseEntity<List<File>> getAll() throws GeneralSecurityException, IOException {
		final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
		Drive service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
				.setApplicationName(APPLICATION_NAME).build();

		// Print the names and IDs for up to 10 files.
		FileList result = service.files().list().setPageSize(10).setFields("nextPageToken, files(id, name)").execute();
		List<File> files = result.getFiles();
		return new ResponseEntity<>(files, HttpStatus.OK);
	}

	@PostMapping("upload")
	public ResponseEntity<String> upload(@RequestParam("file") MultipartFile file)
			throws GeneralSecurityException, IOException {
		final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
		Drive service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
				.setApplicationName(APPLICATION_NAME).build();

		// Print the names and IDs for up to 10 files.
		File upload = new File();
		upload.set((new Date().getTime() + file.getName()), file);
		java.io.File filePath = new java.io.File("cargadoscontrol/" + file.getName());
		FileContent mediaContent = new FileContent(file.getContentType(), filePath);
		File response = service.files().create(upload, mediaContent).setFields("id, parents").execute();
		return new ResponseEntity<>(response.getId(), HttpStatus.OK);
	}

	private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
		// Load client secrets.
		InputStream in = CREDENCIALES.getInputStream();
		if (in == null) {
			throw new FileNotFoundException("Resource not found: " + CREDENCIALES.getFile().getPath());
		}
		GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

		// Build flow and trigger user authorization request.
		List<String> scopes = Collections.singletonList(DriveScopes.DRIVE_METADATA_READONLY);
		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY,
				clientSecrets, scopes)
						.setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
						.setAccessType("offline").build();
		LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
		return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
	}

	@DeleteMapping("delete")
	public ResponseEntity<String> delete(@RequestParam("file") MultipartFile file)
			throws GeneralSecurityException, IOException {
		final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
		Drive service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
				.setApplicationName(APPLICATION_NAME).build();

		// Print the names and IDs for up to 10 files.
		File upload = new File();
		upload.set((new Date().getTime() + file.getName()), file);
		java.io.File filePath = new java.io.File("cargadoscontrol/" + file.getName());
		FileContent mediaContent = new FileContent(file.getContentType(), filePath);
		File response = service.files().create(upload, mediaContent).setFields("id, parents").execute();
		return new ResponseEntity<>(response.getId(), HttpStatus.OK);
	}

	@PostMapping("download/{id}/content-type/{type}")
	public ResponseEntity<OutputStream> download(@PathVariable String id, @PathVariable String type,
			HttpServletResponse response) throws GeneralSecurityException, IOException {
		final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
		Drive service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
				.setApplicationName(APPLICATION_NAME).build();

		OutputStream outputStream = new ByteArrayOutputStream();
		service.files().export(id, type).executeMediaAndDownloadTo(outputStream);

		return new ResponseEntity<>(outputStream, HttpStatus.OK);
	}

}
