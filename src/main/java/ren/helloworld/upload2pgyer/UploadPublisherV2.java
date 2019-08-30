package ren.helloworld.upload2pgyer;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import hudson.util.FormValidation;
import hudson.util.Secret;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import ren.helloworld.upload2pgyer.apiv2.ParamsBeanV2;
import ren.helloworld.upload2pgyer.helper.PgyerV2Helper;

import javax.servlet.ServletException;
import java.io.IOException;

/**
 * upload to jenkins
 *
 * @author myroid
 */
public class UploadPublisherV2 extends Recorder {

    private Secret apiKey;
    private String scanDir;
    private String wildcard;
    private String buildInstallType;
    private Secret buildPassword;
    private String buildUpdateDescription;
    private String buildName;

    private String qrcodePath;
    private String envVarsPath;

    @DataBoundConstructor
    public UploadPublisherV2(String apiKey, String scanDir, String wildcard, String buildName, String buildInstallType, String buildPassword, String buildUpdateDescription, String qrcodePath, String envVarsPath) {
        this.apiKey = Secret.fromString(apiKey);
        this.scanDir = scanDir;
        this.wildcard = wildcard;
        this.buildName = buildName;
        this.buildPassword = Secret.fromString(buildPassword);
        this.buildInstallType = buildInstallType;
        this.buildUpdateDescription = buildUpdateDescription;
        this.qrcodePath = qrcodePath;
        this.envVarsPath = envVarsPath;
    }

    public String getApiKey() {
        return apiKey.getEncryptedValue();
    }

    public String getScanDir() {
        return scanDir;
    }

    public String getWildcard() {
        return wildcard;
    }

    public String getBuildInstallType() {
        return buildInstallType;
    }

    public String getBuildPassword() {
        return buildPassword.getEncryptedValue();
    }

    public String getBuildUpdateDescription() {
        return buildUpdateDescription;
    }

    public String getBuildName() {
        return buildName;
    }

    public String getQrcodePath() {
        return qrcodePath;
    }

    public String getEnvVarsPath() {
        return envVarsPath;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
        ParamsBeanV2 paramsBeanV2 = new ParamsBeanV2();
        paramsBeanV2.setApiKey(apiKey.getPlainText());
        paramsBeanV2.setScandir(scanDir);
        paramsBeanV2.setWildcard(wildcard);
        paramsBeanV2.setBuildPassword(buildPassword.getPlainText());
        paramsBeanV2.setBuildInstallType(buildInstallType);
        paramsBeanV2.setBuildName(buildName);
        paramsBeanV2.setBuildUpdateDescription(buildUpdateDescription);
        paramsBeanV2.setQrcodePath(qrcodePath);
        paramsBeanV2.setEnvVarsPath(envVarsPath);
        return PgyerV2Helper.upload(build, listener, paramsBeanV2);
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    @Symbol("upload-pgyer-v2")
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {
        public DescriptorImpl() {
            load();
        }

        public FormValidation doCheckApiKey(@QueryParameter String value)
                throws IOException, ServletException {
            if (value.length() == 0)
                return FormValidation.error("Please set a api_key");
            if (!value.matches("[A-Za-z0-9]{32}"))
                return FormValidation.warning("Is this correct?");
            return FormValidation.ok();
        }

        public FormValidation doCheckScanDir(@QueryParameter String value)
                throws IOException, ServletException {
            if (value.length() == 0)
                return FormValidation.error("Please set upload ipa or apk file base dir name");
            return FormValidation.ok();
        }

        public FormValidation doCheckWildcard(@QueryParameter String value)
                throws IOException, ServletException {
            if (value.length() == 0)
                return FormValidation.error("Please set upload ipa or apk file wildcard");
            return FormValidation.ok();
        }

        public FormValidation doCheckBuildInstallType(@QueryParameter int value)
                throws IOException, ServletException {
            if (value < 1 || value > 3)
                return FormValidation.error("application installation, the value is (1,2,3).");
            return FormValidation.ok();
        }

        public FormValidation doCheckBuildPassword(@QueryParameter String value)
                throws IOException, ServletException {
            if (value.length() == 0)
                return FormValidation.error("Please set a password");
            if (value.length() < 6)
                return FormValidation.warning("Isn't the password too short?");
            return FormValidation.ok();
        }

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        public String getDisplayName() {
            return "Upload to pgyer with apiV2";
        }
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }
}

