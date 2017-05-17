package play.deps;
import org.apache.ivy.core.cache.DefaultRepositoryCacheManager;
import org.apache.ivy.core.module.descriptor.Artifact;
import org.apache.ivy.core.cache.ArtifactOrigin;

public class LocalModuleRepositoryCacheManager extends DefaultRepositoryCacheManager {
  public ArtifactOrigin getSavedArtifactOrigin(Artifact artifact) {
    return ArtifactOrigin.unkwnown(artifact);
  }
}