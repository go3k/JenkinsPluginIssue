package org.go3k.highlordconf;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import hudson.Extension;
import hudson.ExtensionPoint;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.tasks.BuildWrapperDescriptor;
import hudson.util.ListBoxModel;
import jenkins.model.Jenkins;

import org.kohsuke.stapler.DataBoundConstructor;

public abstract class ConfigType implements ExtensionPoint, Describable<ConfigType> {
    protected String name;
    protected ConfigType(String name) { this.name = name; }

    public Descriptor<ConfigType> getDescriptor() {
        return Jenkins.getInstance().getDescriptor(getClass());
    }

    public static class ConfigTypeDescriptor extends Descriptor<ConfigType> {
        public ConfigTypeDescriptor(Class<? extends ConfigType> clazz) {
            super(clazz);
        }
        public String getDisplayName() {
            return clazz.getSimpleName();
        }
    }

    public static class FileConfigType extends ConfigType {
        private String configFile;
        
        @DataBoundConstructor 
        public FileConfigType(String configFile) {
            super("FileConfigType");
            this.configFile = configFile;
        }
        
        public String getConfigFile() {
            return configFile;
        }
        public String getDefaultConfigValue() {
        	String confFolder = Helper.Instance().getMainBuilder().getDescriptor().getConfigsFolder();
        	String filename = confFolder + (confFolder.endsWith("/") ? "" : "/") + configFile;
        	if (filename.length() > 0)
        	{
        		File file = new File(filename);
        		return Helper.Read2String(file);
        	}
            return "Hello. hello";
        }
        
//        @Extension public final ConfigTypeDescriptor D = new ConfigTypeDescriptor(FileConfigType.class);
        @Extension
        public static class FileConfigTypeDescriptor extends ConfigTypeDescriptor {
        	private List<String> allConfigFiles = null;
        	
        	public FileConfigTypeDescriptor() {
        		super(FileConfigType.class);
            }
        	
        	public String getDisplayName() {
                return "hello this is FileConfigType";
            }
        	
            
            private List<String> GetAllConfigFiles()
            {
            	if (allConfigFiles == null)
            	{
            		String configsFolder = Helper.Instance().getMainBuilder().getDescriptor().getConfigsFolder();
            		
            		allConfigFiles = new ArrayList<String>();
                	File dir = new File(configsFolder);
                    File file[] = dir.listFiles();
                    for (int i = 0; i < file.length; i++) {
                    	if (!file[i].isFile()) continue;
                    	
                    	String name = file[i].getName();
                    	if (name.startsWith(".")) continue;
                    	allConfigFiles.add(name);
                    }
            	}
            	
            	return allConfigFiles;
            }

            public ListBoxModel doFillConfigFileItems() {
                ListBoxModel items = new ListBoxModel();
                for (String file : GetAllConfigFiles()) {
                    items.add(file, file);
                }
                return items;
            }
        }
    }

    public static class CustomConfigType extends ConfigType {
        private boolean yellow;
        @DataBoundConstructor 
        public CustomConfigType(boolean yellow) {
            super("CustomConfigType");
            this.yellow = yellow;
        }
        @Extension public static final ConfigTypeDescriptor D = new ConfigTypeDescriptor(CustomConfigType.class);
    }
}

