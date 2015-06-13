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
    private String configFile;
    private boolean modify;
    private String configValue;

    // Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
    @DataBoundConstructor
    public HelloWorldBuilder(ConnectType connectType, String configFile, boolean modify, String configValue) {
        this.connectType = connectType;
        this.configFile = configFile;
        this.modify = modify;
        this.configValue = configValue;
        
//        this.configValue = "hello hello hello.";
    }
    
    public ConnectType getConnectType() {
        return connectType;
    }
    public String getConfigFile() {
        return configFile;
    }
    public boolean getModify() {
        return modify;
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
                		+ " modify: " + modify
                		+ " configValue: " + configValue);
            }
        };
    }
//    @Override
//    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {
//        // This is where you 'build' the project.
//        // Since this is a dummy, we just say 'hello world' and call that a build.
//
//        // This also shows how you can consult the global configuration of the builder
//        if (getDescriptor().getUseFrench())
//            listener.getLogger().println("Bonjour, "+name+"!");
//        else
//            listener.getLogger().println("Hello, "+name+"!");
//        
//        return true;
//    }

    // Overridden for better type safety.
    // If your plugin doesn't really define any property on Descriptor,
    // you don't have to do this.
    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl)super.getDescriptor();
    }

    /**
     * Descriptor for {@link HelloWorldBuilder}. Used as a singleton.
     * The class is marked as public so that it can be accessed from views.
     *
     * <p>
     * See <tt>src/main/resources/hudson/plugins/hello_world/HelloWorldBuilder/*.jelly</tt>
     * for the actual HTML fragment for the configuration screen.
     */
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
        
//        public FormValidation doCheckConfigFile(@QueryParameter String file)
//                throws IOException, ServletException {
//        	List<String> list = GetAllConfigFiles();
//        	
//            return FormValidation.ok();
//        }

//        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
//            // Indicates that this builder can be used with all kinds of project types 
//            return true;
//        }

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

        /**
         * This method returns true if the global configuration says we should speak French.
         *
         * The method name is bit awkward because global.jelly calls this method to determine
         * the initial state of the checkbox by the naming convention.
         */
        public String getConfigsFolder() {
            return configsFolder;
        }
    }
}

