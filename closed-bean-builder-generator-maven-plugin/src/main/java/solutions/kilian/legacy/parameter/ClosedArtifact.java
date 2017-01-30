package solutions.kilian.legacy.parameter;

import org.sonatype.aether.util.artifact.DefaultArtifact;

public class ClosedArtifact {
    private String groupId;
    private String artifactId;
    private String version;
    private boolean dependency;

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public DefaultArtifact artifact() {
        return new DefaultArtifact(groupId + ":" + artifactId + ":" + this.version);
    }

    public boolean isDependency() {
        return dependency;
    }

    public void setDependency(boolean dependency) {
        this.dependency = dependency;
    }

}
