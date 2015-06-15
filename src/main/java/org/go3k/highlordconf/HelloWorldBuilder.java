package org.go3k.highlordconf;
import hudson.DescriptorExtensionList;
import hudson.Extension;
import hudson.ExtensionPoint;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.Describable;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Descriptor;
import hudson.tasks.BuildWrapper;
import hudson.tasks.BuildWrapperDescriptor;
import hudson.tasks.Builder;
import hudson.util.ListBoxModel;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jenkins.model.Jenkins;
import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

/**
 * Sample {@link Builder}.
 *
 * <p>
 * When the user configures the project and enables this builder,
 * {@link DescriptorImpl#newInstance(StaplerRequest)} is invoked
 * and a new {@link HelloWorldBuilder} is created. The created
 * instance is persisted to the project configuration XML by using
 * XStream, so this allows you to use instance fields (like {@link #name})
 * to remember the configuration.
 *
 * <p>
 * When a build is performed, the {@link #perform(AbstractBuild, Launcher, BuildListener)}
 * method will be invoked. 
 *
 * @author Kohsuke Kawaguchi
 */
public class HelloWorldBuilder extends BuildWrapper {
    private ConnectType connectType;
    private ConfigType fruit;
//    private String configType;
//    private String configFile;
//    private String configValue;

    // Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
    @DataBoundConstructor
    public HelloWorldBuilder(ConnectType connectType, ConfigType fruit) {
        this.connectType = connectType;
        this.fruit = fruit;
//        this.configType = configType;
//        this.configFile = configFile;
//        this.configValue = configValue;
        
        Helper.Instance().setMainBuilder(this);
    }
    
    public ConnectType getConnectType() {
        return connectType;
    }
//    public String getConfigType() {
//        return configType;
//    }
//    public String getConfigFile() {
//        return configFile;
//    }
//    public String getConfigValue() {
//        return configValue;
//    }
    public DescriptorExtensionList<ConfigType,Descriptor<ConfigType>> getConfigTypeDescriptors() {
        return Jenkins.getInstance().<ConfigType,Descriptor<ConfigType>>getDescriptorList(ConfigType.class);
    }
    public ConfigType getFruit() {
    	return fruit;
    }

    @Override
    public Environment setUp(AbstractBuild build, Launcher launcher, final BuildListener listener) throws IOException, InterruptedException {
    	listener.getLogger().println("setup env.");
    	return new Environment() {
            public @Override void buildEnvVars(Map<String,String> env) {
                env.put("hltest", "Hellohello.");
                String confFolder = getDescriptor().getConfigsFolder();
                listener.getLogger().println("All Vars: " + confFolder + " connectType: " + connectType 
//                		+ " configFile: " + configFile
//                		+ " configType: " + configType
//                		+ " configValue: " + configValue
                		);
            }
        };
    }
    
    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl)super.getDescriptor();
    }

    @Extension // This indicates to Jenkins that this is an implementation of an extension point.
    public static final class DescriptorImpl extends BuildWrapperDescriptor {
    	
    	public boolean isApplicable(AbstractProject item) {
            return true;
        }
    	
        private String configsFolder;
//        private List<String> allConfigFiles = null;
        
        public DescriptorImpl() {
            load();
        }
        
        public String getDisplayName() {
            return "项目配置选项";
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            // To persist global configuration information,
            // set that to properties and call save().
        	configsFolder = formData.getString("configsFolder");
            // ^Can also use req.bindJSON(this, formData);
            //  (easier when there are many fields; need set* methods for this, like setUseFrench)
            save();
            return super.configure(req,formData);
        }
        
        public String getConfigsFolder() {
            return configsFolder;
        }
    }
    
    //Inner classes
    public static abstract class ConfigType implements ExtensionPoint, Describable<ConfigType> {
        protected String name;
        protected ConfigType(String name) { this.name = name; }

        public ConfigTypeDescriptor getDescriptor() {
            return (ConfigTypeDescriptor)Jenkins.getInstance().getDescriptor(getClass());
        }
    }

    public static class ConfigTypeDescriptor extends Descriptor<ConfigType> {
        public ConfigTypeDescriptor(Class<? extends ConfigType> clazz) {
            super(clazz);
        }
        public String getDisplayName() {
            return clazz.getSimpleName();
        }
        
        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            save();
            return super.configure(req,formData);
        }
    }

    public static class FileConfigType extends ConfigType {
        private String configFile;
        
        @DataBoundConstructor 
        public FileConfigType(String configFile) {
            super("选择配置文件");
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
//        		return filename;
        		File file = new File(filename);
        		return Helper.Read2String(file);
        	}
            return "Hello. hello";
        }
        
        
//        @Extension public static final ConfigTypeDescriptor D = new ConfigTypeDescriptor(FileConfigType.class);
        
        public FileConfigTypeDescriptor getDescriptor() {
            return (FileConfigTypeDescriptor)super.getDescriptor();
        }
        @Extension
        public static final class FileConfigTypeDescriptor extends ConfigTypeDescriptor {
        	private List<String> allConfigFiles = null;
        	
        	public FileConfigTypeDescriptor() {
        		super(FileConfigType.class);
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
            
            @Override
            public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
                save();
                return super.configure(req,formData);
            }
        }
    }

    public static class CustomConfigType extends ConfigType {
        private String custom;
        @DataBoundConstructor 
        public CustomConfigType(String custom) {
            super("CustomConfigType");
            this.custom = custom;
        }
        @Extension public static final ConfigTypeDescriptor D = new ConfigTypeDescriptor(CustomConfigType.class);
    }
    
}

