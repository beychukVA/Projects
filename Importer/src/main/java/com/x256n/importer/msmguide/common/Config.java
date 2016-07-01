package com.x256n.importer.msmguide.common;

import java.io.File;
import java.util.Locale;

/**
 * @author Aleksey Permyakov (03.12.2015).
 */
public class Config {
    private File rootDirectory;
    private File typeDirectory;
    private File itemDirectory;
    private File itemFile;

    public Config() {
    }

    public Config(File itemFile) {
        setItemFile(itemFile);
    }

    public File getRootDirectory() {
        return rootDirectory;
    }

    public void setRootDirectory(File rootDirectory) {
        this.rootDirectory = rootDirectory;
        this.typeDirectory = null;
        this.itemDirectory = null;
        this.itemFile = null;
    }

    public File getTypeDirectory() {
        return typeDirectory;
    }

    public void setTypeDirectory(File typeDirectory) {
        this.typeDirectory = typeDirectory;

        this.rootDirectory = this.typeDirectory.getParentFile();

        this.itemDirectory = null;
        this.itemFile = null;
    }

    public File getItemDirectory() {
        return itemDirectory;
    }

    public void setItemDirectory(File itemDirectory) {
        this.itemDirectory = itemDirectory;

        this.typeDirectory = this.itemDirectory.getParentFile();
        this.rootDirectory = this.typeDirectory.getParentFile();

        this.itemFile = null;
    }

    public File getItemFile() {
        return itemFile;
    }

    public void setItemFile(File itemFile) {
        this.itemFile = itemFile;

        this.itemDirectory = this.itemFile.getParentFile();
        this.typeDirectory = this.itemDirectory.getParentFile();
        this.rootDirectory = this.typeDirectory.getParentFile();
    }

    public File getIconDirectory() {
        return new File(itemDirectory.getAbsolutePath() + File.separator + IConstants._ICONS);
    }

    public File getIconFile(String iconGuid) {
        final File iconDirectory = getIconDirectory();
        if ((getTypeDirectory().exists() || getTypeDirectory().mkdirs()) && (iconDirectory.exists() || iconDirectory.mkdirs())) {
            return new File(iconDirectory.getAbsolutePath() + File.separator + iconGuid + IConstants._ICON_EXT);
        }
        return null;
    }

    public File getItemGuidFile() {
        final String itemName = getItemFile().getName();
        return new File(getItemDirectory().getAbsolutePath() + File.separator + String.format(Locale.CANADA, IConstants._GUID_FILE, itemName));
    }

    public File getDirectoryGuidFile() {
        final String itemName = getItemDirectory().getName();
        return new File(getTypeDirectory().getAbsolutePath() + File.separator + String.format(Locale.CANADA, IConstants._GUID_FILE, itemName));
    }
}
