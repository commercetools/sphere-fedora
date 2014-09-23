
import com.google.inject.Guice;
import com.google.inject.Injector;
import play.Application;
import play.GlobalSettings;

public class Global extends GlobalSettings {

    private Injector injector;

    @Override
    public void onStart(final Application application) {
        super.onStart(application);
        injector = createInjector(application);
    }

    @Override
    public <A> A getControllerInstance(final Class<A> controllerClass) throws Exception {
        return injector.getInstance(controllerClass);
    }

    private Injector createInjector(final Application application) {
        return Guice.createInjector(new ProductionModule(application));
    }
}
