package strobeyworks.render.scenes;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import strobeyworks.object.Mesh;
import strobeyworks.object.SceneObject;
import strobeyworks.platform.Animation;
import strobeyworks.render.lightsources.DirectionalLight;
import strobeyworks.render.lightsources.LightSource;
import strobeyworks.render.lightsources.SpotLight;

public abstract class Scene {
    protected Set<Mesh> meshes;
    protected Set<SceneObject> objects;
    protected Set<Animation> animations;

    protected List<LightSource> lights;
    protected List<DirectionalLight> directionalLights;
    protected List<SpotLight> spotLights;
    protected List<SceneObject> lightSpheres;

    private float ambientStrength;
    private float specularStrength;
    private float specularShininess;
    
    public Scene() {
        meshes = new HashSet<>();
        objects = new HashSet<>();
        lights = new ArrayList<>();
        directionalLights = new ArrayList<>();
        spotLights = new ArrayList<>();
        lightSpheres = new ArrayList<>();
        animations = new HashSet<>();

        ambientStrength = 0.0f;
        specularStrength = 0.5f;
        specularShininess = 32f;
    }

    public abstract void init();

    public void update() {
        for (Animation a : animations) a.trigger();
    };

    protected void addObject(SceneObject o) {
        meshes.add(o.getMesh());
        objects.add(o);
    }

    protected void addLight(LightSource l) {
        lights.add(l);

        if (l instanceof DirectionalLight) directionalLights.add((DirectionalLight) l);
        else if (l instanceof SpotLight) spotLights.add((SpotLight) l);
    }

    public Set<Mesh> getMeshes() {return meshes;}
    public Set<SceneObject> getObjects() {return objects;}
    public Set<Animation> getAnimations() {return animations;}

    public List<LightSource> getAllLights() {return lights;}
    public List<DirectionalLight> getDirectionalLights() {return directionalLights;}
    public List<SpotLight> getSpotLights() {return spotLights;}

    public float getAmbientStrength() {return ambientStrength;}
    public float getSpecularStrength() {return specularStrength;}
    public float getSpecularShininess() {return specularShininess;}
}
