package org.go3k.highlordconf;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.tasks.BuildWrapper;
import hudson.tasks.BuildWrapperDescriptor;
import hudson.tasks.Builder;
import hudson.util.ListBoxModel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    private String configType;
    private String configFile;
    private String configValue;

    // Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
    @DataBoundConstructor
    public HelloWorldBuilder(ConnectType connectType, String configType, String configFile, String configValue) {
        this.connectType = connectType;
        this.configType = configType;
        this.configFile = configFile;
        this.configValue = configValue;
        
    }
    
    public ConnectType getConnectType() {
        return connectType;
    }
    public String getConfigType() {
        return configType;
    }
    public String getConfigFile() {
        return configFile;
    }
    public String getConfigValue() {
        return configValue;
    }
    public String getDefaultConfigValue() {
    	String confFolder = getDescriptor().getConfigsFolder();
    	String filename = confFolder + (confFolder.endsWith("/") ? "" : "/") + configFile;
    	if (filename.length() > 0)
    	{
    		File file = new File(filename);
    		return Read2String(file);
    	}
        return "Hello. hello";
    }
    
    public static String Read2String(File file){
        String result = "";
        try{
            BufferedReader br = new BufferedReader(new FileReader(file));//构造一个BufferedReader类来读取文件
            String s = null;
            while((s = br.readLine())!=null){//使用readLine方法，一次读一行
                result = result + "\n" +s;
            }
            br.close();    
        }catch(Exception e){
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public Environment setUp(AbstractBuild build, Launcher launcher, final BuildListener listener) throws IOException, InterruptedException {
    	listener.getLogger().println("setup env.");
    	return new Environment() {
            public @Override void buildEnvVars(Map<String,String> env) {
                env.put("hltest", "Hellohello.");
                String confFolder = getDescriptor().getConfigsFolder();
                listener.getLogger().println("All Vars: " + confFolder + " connectType: " + connectType 
                		+ " configFile: " + configFile
                		+ " configType: " + configType
                		+ " configValue: " + configValue);
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
        private List<String> allConfigFiles = null;
        
        public DescriptorImpl() {
            load();
        }
        
        private List<String> GetAllConfigFiles()
        {
        	if (allConfigFiles == null)
        	{
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

        /**
         * This human readable name is used in the configuration screen.
         */
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
}

