// SPDX-License-Identifier: MIT

package lermitage.intellij.extra.icons.cfg;

import com.intellij.notification.*;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import lermitage.intellij.extra.icons.ExtraIconProvider;
import lermitage.intellij.extra.icons.Globals;
import lermitage.intellij.extra.icons.Model;
import lermitage.intellij.extra.icons.cfg.services.impl.SettingsIDEService;
import lermitage.intellij.extra.icons.cfg.services.impl.SettingsProjectService;
import lermitage.intellij.extra.icons.providers.Angular2IconProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public abstract class SettingsService {

    private static final Logger LOGGER = Logger.getInstance(SettingsService.class);

    // the implementation of PersistentStateComponent works by serializing public fields, so keep them public
    @SuppressWarnings("WeakerAccess")
    public List<String> disabledModelIds = new ArrayList<>();
    @SuppressWarnings("WeakerAccess")
    public String ignoredPattern;
    @SuppressWarnings("WeakerAccess")
    public List<Model> customModels = new ArrayList<>();
    @SuppressWarnings("WeakerAccess")
    public Double additionalUIScale;

    private Pattern ignoredPatternObj;
    private Boolean isIgnoredPatternValid;

    public List<String> getDisabledModelIds() {
        if (disabledModelIds == null) { // a malformed xml file could make it null
            disabledModelIds = new ArrayList<>();
        }
        return disabledModelIds;
    }

    public String getIgnoredPattern() {
        return ignoredPattern;
    }

    public Pattern getIgnoredPatternObj() {
        if (isIgnoredPatternValid == null) {
            compileAndSetRegex(ignoredPattern);
        }
        if (isIgnoredPatternValid == Boolean.TRUE) {
            return ignoredPatternObj;
        }
        return null;
    }

    public void setDisabledModelIds(List<String> disabledModelIds) {
        this.disabledModelIds = disabledModelIds;
    }

    public void setIgnoredPattern(String ignoredPattern) {
        this.ignoredPattern = ignoredPattern;
        compileAndSetRegex(ignoredPattern);
    }

    public List<Model> getCustomModels() {
        if (customModels == null) { // a malformed xml file could make it null
            customModels = new ArrayList<>();
        }
        return customModels;
    }

    public void setCustomModels(List<Model> customModels) {
        this.customModels = customModels;
    }

    public Double getAdditionalUIScale() {
        if (additionalUIScale == null) {
            additionalUIScale = (double) 1.0f;
        }
        return additionalUIScale;
    }

    public void setAdditionalUIScale(Double additionalUIScale) {
        this.additionalUIScale = additionalUIScale;
    }

    @NotNull
    public static List<Model> getAllRegisteredModels() {
        List<Model> allModels = new ArrayList<>();
        allModels.addAll(ExtraIconProvider.allModels());
        allModels.addAll(Angular2IconProvider.allModels());
        return allModels;
    }

    @NotNull
    public static SettingsService getInstance(@Nullable Project project) {
        if (project == null) {
            return getIDEInstance();
        }
        return ServiceManager.getService(project, SettingsProjectService.class);
    }

    @NotNull
    public static SettingsService getIDEInstance() {
        return ServiceManager.getService(SettingsIDEService.class);
    }

    private void compileAndSetRegex(String regex) {
        if (regex != null && !regex.isEmpty()) {
            try {
                ignoredPatternObj = Pattern.compile(regex);
                isIgnoredPatternValid = true;
            } catch (PatternSyntaxException e) {
                // TODO when switching to 2020.3 as minimal version: rework NotificationGroup creation
                //  https://jetbrains.org/intellij/sdk/docs/user_interface_components/notifications.html
                //  (see "NotificationGroup (2020.3 and later)")
                NotificationGroup notificationGroup = new NotificationGroup(
                    Globals.PLUGIN_GROUP_DISPLAY_ID,
                    NotificationDisplayType.BALLOON,
                    true
                );
                Notification notification = notificationGroup.createNotification(
                    Globals.PLUGIN_NAME + " settings",
                    null,
                    "Can't compile regex: '" + regex + "' (" + e.getMessage() + ")",
                    NotificationType.WARNING
                );
                notification.setImportant(true);
                Notifications.Bus.notify(notification);
                LOGGER.warn("Can't compile regex '" + regex + "'");
                ignoredPatternObj = null;
                isIgnoredPatternValid = false;
            }
        }
    }
}
