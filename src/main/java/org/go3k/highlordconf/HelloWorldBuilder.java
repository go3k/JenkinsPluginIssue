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

import java.io.IOException;
import java.util.Map;

import jenkins.model.Jenkins;
import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

public class HelloWorldBuilder extends BuildWrapper {
    private Fruit fruit;

    // Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
    @DataBoundConstructor
    public HelloWorldBuilder(Fruit fruit) {
        this.fruit = fruit;
    }
    public Fruit getFruit() {
        // Could return currently configured/saved item here to initialized form with this data
        return fruit;
    }

    public DescriptorExtensionList<Fruit,Descriptor<Fruit>> getFruitDescriptors() {
        return Jenkins.getInstance().<Fruit,Descriptor<Fruit>>getDescriptorList(Fruit.class);
    }

    @Override
    public Environment setUp(AbstractBuild build, Launcher launcher, final BuildListener listener) throws IOException, InterruptedException {
    	listener.getLogger().println("setup env.");
    	return new Environment() {
            public @Override void buildEnvVars(Map<String,String> env) {
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
        
        public DescriptorImpl() {
            load();
        }
        
        public String getDisplayName() {
            return "config";
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            save();
            return true;
        }
        
    }
    
    public static abstract class Fruit implements ExtensionPoint, Describable<Fruit> {
        protected String name;
        protected Fruit(String name) { this.name = name; }

        public Descriptor<Fruit> getDescriptor() {
            return Jenkins.getInstance().getDescriptor(getClass());
        }
    }

    public static class FruitDescriptor extends Descriptor<Fruit> {
        public FruitDescriptor(Class<? extends Fruit> clazz) {
            super(clazz);
        }
        public String getDisplayName() {
            return clazz.getSimpleName();
        }
    }

    public static class Apple extends Fruit {
        private int seeds;
        @DataBoundConstructor public Apple(int seeds) {
            super("Apple");
            this.seeds = seeds;
        }
        @Extension public static final FruitDescriptor D = new FruitDescriptor(Apple.class);
    }

    public static class Banana extends Fruit {
        private boolean yellow;
        @DataBoundConstructor public Banana(boolean yellow) {
            super("Banana");
            this.yellow = yellow;
        }
        @Extension public static final FruitDescriptor D = new FruitDescriptor(Banana.class);
    }
}

