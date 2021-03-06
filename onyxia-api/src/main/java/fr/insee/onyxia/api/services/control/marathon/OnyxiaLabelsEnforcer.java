package fr.insee.onyxia.api.services.control.marathon;

import fr.insee.onyxia.api.services.control.AdmissionControllerMarathon;
import fr.insee.onyxia.api.services.control.utils.PublishContext;
import fr.insee.onyxia.model.User;
import fr.insee.onyxia.model.catalog.UniversePackage;
import fr.insee.onyxia.model.region.Region;
import mesosphere.marathon.client.model.v2.App;
import mesosphere.marathon.client.model.v2.Group;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class OnyxiaLabelsEnforcer implements AdmissionControllerMarathon {

    @Override
    public boolean validateContract(Region region, Group group, App app, User user, UniversePackage pkg, Map<String,Object> configData, PublishContext context) {

        Map<String, String> onyxiaOptions = (Map<String,String>) configData.get("onyxia");
        app.addLabel("ONYXIA_NAME", pkg.getName());
        if (onyxiaOptions != null) {
            app.addLabel("ONYXIA_TITLE", onyxiaOptions.get("friendlyName"));
        }

        if (group != null && StringUtils.isNotBlank(app.getId())) {
            app.addLabel("ONYXIA_SUBTITLE", StringUtils.substringAfterLast(app.getId(),"/"));
        }
        else {
            app.addLabel("ONYXIA_SUBTITLE", pkg.getName());
        }

        app.addLabel("ONYXIA_SCM", pkg.getScm());
        app.addLabel("ONYXIA_DESCRIPTION", pkg.getDescription());
        if (!app.getLabels().containsKey("ONYXIA_URL") && app.getLabels().containsKey("HAPROXY_0_VHOST")) {
            if (app.getLabels().containsKey("HAPROXY_1_VHOST")) {
                app.addLabel("ONYXIA_URL", "https://" + app.getLabels().get("HAPROXY_0_VHOST") + ",https://"
                        + app.getLabels().get("HAPROXY_1_VHOST"));
            } else {
                app.addLabel("ONYXIA_URL", "https://" + app.getLabels().get("HAPROXY_0_VHOST"));
            }
        }
        app.addLabel("ONYXIA_LOGO", (String) ((Map<String, Object>) pkg.getResource().get("images")).get("icon-small"));
        if (region.getServices().getMonitoring() != null) {
            String slugId = app.getId();
            if (slugId.startsWith("/")) {
                slugId = slugId.substring(1);
            }
            slugId = slugId.replaceAll("/","_");
            String monitoringUrl = region.getServices().getMonitoring().getUrlPattern().replaceAll("\\$appIdSlug",slugId);
            app.addLabel("ONYXIA_MONITORING", monitoringUrl);
        }

        app.addLabel("ONYXIA_POST_INSTALL_INSTRUCTIONS", pkg.getPostInstallNotes());

        return true;
    }

    @Override
    public Integer getPriority() {
        return -1000;
    }
}
