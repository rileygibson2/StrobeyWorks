package strobeyworks.render.scenes;

import strobeyworks.SWMain;
import strobeyworks.object.Mesh;
import strobeyworks.object.Mesh.MeshType;
import strobeyworks.platform.Animation;
import strobeyworks.render.ObjLoader;
import strobeyworks.render.lightsources.DirectionalLight;
import strobeyworks.render.lightsources.LightSource;
import strobeyworks.render.lightsources.SpotLight;
import strobeyworks.object.SceneObject;
import strobeyworks.utils.Vec2;
import strobeyworks.utils.Vec3;

public class WorkingScene extends Scene {
    
    private SceneObject obj1;
    private SpotLight spot1;
    private SpotLight spot2;
    private Vec3 lightColor = new Vec3(1f, 0.6f, 0.3f);
    
    public WorkingScene() {
        super();
    }
    
    @Override
    public void initialise() {
        LightSource light = new DirectionalLight(
            new Vec3(-1f, -0.2f, 0f),
            lightColor,
            0.4f
        );
        //light.enableShadow(false);
        addLight(light);
        
        spot1 = new SpotLight(
            new Vec3(0f, 8f, 0f),
            new Vec3(0f, -1f, 0f),
            10f,
            new Vec3(1f, 1f, 1f),
            1f
        );
        spot1.softenEdge(0.3f);
        spot1.setWidth(15f);
        spot1.setThrowDistance(200f);
        spot1.setPosition(new Vec3(0f, 20f, 1f));
        spot1.pointAt(new Vec3(0f, 1f, 0f));
        addLight(spot1);

        spot2 = new SpotLight(
            new Vec3(0f, 8f, 0f),
            new Vec3(0f, -1f, 0f),
            20f,
            new Vec3(1f, 1f, 1f),
            1f
        );
        spot2.softenEdge(0.3f);
        spot2.setWidth(10f);
        spot2.setThrowDistance(200f);
        //spotLight.setPosition(new Vec3(0f, 8f, 1f));
        //spotLight.pointAt(new Vec3(0f, 1f, 0f));
        //addLight(spot2);
        
        Animation beamPulse = new Animation(1, Animation.AnimationForm.SINE, (i, value) -> {
            getSpotLights().get(i).setWidth(value);
        });
        beamPulse.setMinMax(20f, 30f);
        beamPulse.setSpeed(0.02f);
        //animations.add(beamPulse);
        
        
        Animation a = new Animation(getSpotLights().size(), Animation.AnimationForm.SINE, (i, value) -> {
            getSpotLights().get(i).setIntensity(value);
        });
        a.setWidth(1f);
        a.setSpeed(0.02f);
        a.setMinMax(0.1f, 1f);
        //a.shufflePhase();
        //a.setPhase(0f, 0.8f);
        //animations.add(a);

        a = new Animation(getSpotLights().size(), Animation.AnimationForm.SINE, (i, value) -> {
            getSpotLights().get(i).setRed(value);
            getSpotLights().get(i).setGreen(0f);
        });
        a.setWidth(1f);
        a.setSpeed(0.02f);
        //animations.add(a);
        
        //Mesh m1 = ObjLoader.loadMesh("teapot.obj", false, MeshType.SMOOTH_SHADED);
        Mesh bbM = ObjLoader.loadMesh("basketball.obj", false, MeshType.SMOOTH_SHADED);
        Mesh pM = ObjLoader.loadMesh("plane.obj", false, MeshType.FLAT_SHADED);

        obj1 = new SceneObject(bbM, new Vec3(0f, 2f, 0f));
        obj1.setScale(new Vec3(1.2f));
        //obj1.setRotation(new Vec3(0f, 90f, 0f).toRadians());
        addObject(obj1);
        
        SceneObject bb = new SceneObject(bbM, new Vec3(-4f, 1f, 3f));
        bb.setScale(new Vec3(0.8f));
        addObject(bb);
        
        bb = new SceneObject(bbM, new Vec3(6f, 1f, -5f));
        bb.setScale(new Vec3(0.4f));
        addObject(bb);
        
        SceneObject plane = new SceneObject(pM, new Vec3(0f, 1f, 0f));
        plane.setScale(new Vec3(8f));
        addObject(plane);
    }
    
    public void update() {
        float dt = SWMain.getDeltaTime();
        float rot = obj1.getRotation().y + 0.2f*dt;
        obj1.setRotation(new Vec3((float) Math.toRadians(0f), rot , 0f));
        
        float lightAngle = SWMain.getTotalTime() * 0.5f;
;
        Vec2 circ = Animation.circlePosition(30.0f, lightAngle);
        //spot1.setPosition(new Vec3(circ.x, 5f, circ.y));
       // spot1.pointAt(new Vec3(0f, 2f, 0f));

        circ = Animation.circlePosition(30.0f, lightAngle+4f);
        //spot2.setPosition(new Vec3(circ.x, 5f, circ.y));
        //spot2.pointAt(new Vec3(0f, 2f, 0f));
        
        //for (int i=0; i<lights.size(); i++) lightSpheres.get(i).setPosition(lights.get(i).getPosition());
        super.update();
    }
}
