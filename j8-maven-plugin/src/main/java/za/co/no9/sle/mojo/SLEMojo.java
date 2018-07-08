package za.co.no9.sle.mojo;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;


@Mojo(name = "j8-sle")
public class SLEMojo extends AbstractMojo {
    @Parameter(defaultValue = "${project.basedir}/src/main/sle", alias = "sourceDirectory")
    private String sourceDirectory;

    @Parameter(defaultValue = "${project.basedir}/target/generated-sources/sle/java", alias = "outputDirectory")
    private String outputDirectory;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        BuildKt.build(getLog(), new java.io.File(sourceDirectory), new java.io.File(outputDirectory));
    }
}

