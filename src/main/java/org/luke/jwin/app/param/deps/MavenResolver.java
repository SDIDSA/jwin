package org.luke.jwin.app.param.deps;

import java.io.File;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.Semaphore;

import org.luke.gui.controls.alert.AlertType;
import org.luke.gui.controls.alert.ButtonType;
import org.luke.gui.exception.ErrorHandler;
import org.luke.jwin.app.Command;
import org.luke.jwin.app.Jwin;
import org.luke.jwin.app.JwinActions;
import org.luke.jwin.app.file.FileDealer;
import org.luke.jwin.app.param.JdkParam;
import org.luke.jwin.local.LocalStore;
import org.luke.jwin.local.managers.JdkManager;
import org.luke.jwin.local.managers.LocalInstall;

public class MavenResolver {
	public static List<File> resolve(File pom) {

        File defaultJdk = new File(LocalStore.getDefaultJdk());

		JdkParam jdkParam = Jwin.instance.getConfig().getJdk();

		File projectJdk = jdkParam.isJdk() ? jdkParam.getValue() : null;

		if (!defaultJdk.exists() && projectJdk == null) {
			LocalInstall jdk = JdkManager.maxVersion();
			if(jdk != null) {
				defaultJdk = jdk.getRoot();
				jdkParam.setFile(defaultJdk);
			} else {
				Semaphore s = new Semaphore(0);
				JwinActions.alert("No Jdk Selected", "jdk is required to resolve maven dependencies, set default jdk now ?",
						AlertType.ERROR, res -> {
							if (res == ButtonType.YES) {
								Jwin.instance.openSettings("jdk_versions");
							}
							s.release();
						}, ButtonType.CANCEL, ButtonType.YES);

				s.acquireUninterruptibly();
				defaultJdk = new File(LocalStore.getDefaultJdk());
			}
			if (!defaultJdk.exists()) {
				return null;
			}
		}

		File jdk = projectJdk != null ? projectJdk : defaultJdk;
		try {
			File temp = new File(
					System.getProperty("java.io.tmpdir") + "/jwin_lib_dep_" + new Random().nextInt(999999));

			temp.mkdir();

			File mvn = new File(URLDecoder.decode(MavenResolver.class.getResource("/mvn/bin/mvn.cmd").getFile(),
					Charset.defaultCharset()));

			File mvnRoot = new File(
					System.getProperty("java.io.tmpdir") + "/jwin_mvn_root_" + new Random().nextInt(9999));
			mvnRoot.mkdir();

			JwinActions.copyDirCont(mvn.getParentFile().getParentFile(), mvnRoot, null);

			File tempMvn = new File(mvnRoot.getAbsolutePath().concat("/bin/mvn.cmd"));

			String toReplace = "@REM ==== START VALIDATION ====";
			FileDealer.write(FileDealer.read(tempMvn).replace(toReplace, toReplace + "\nset JAVA_HOME=" + jdk),
					new File(tempMvn.getAbsolutePath().replace("mvn.cmd", "cmvn.cmd")));

			Command command = new Command("cmd.exe", "/C",
					"cmvn -f \"" + pom.getAbsolutePath() + "\" dependency:copy-dependencies -DoutputDirectory=\""
							+ temp.getAbsolutePath() + "\" -Dhttps.protocols=TLSv1.2");

			command.execute(tempMvn.getParentFile()).waitFor();

            return new ArrayList<>(Arrays.asList(Objects.requireNonNull(temp.listFiles())));
		} catch (Exception x) {
			ErrorHandler.handle(x, "resolving dependencies");
			JwinActions.error("resolve_fail_head", "resolve_fail_body");
			return null;
		}
	}
}
