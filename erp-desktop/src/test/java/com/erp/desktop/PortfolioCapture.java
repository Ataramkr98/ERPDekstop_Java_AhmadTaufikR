package com.erp.desktop;

import java.nio.file.*;
import java.util.concurrent.*;
import java.util.function.BooleanSupplier;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.WritableImage;
import javafx.stage.Stage;
import javafx.stage.Window;
import com.erp.desktop.api.*;
import com.erp.desktop.view.*;

/**
 * Drives the real JavaFX client and captures its scenes.
 *
 * <p>Two flows, selected by {@code ERP_CAPTURE}:
 * <ul>
 *   <li><b>default</b> - the normal walkthrough: sign-in, blank-credential validation,
 *       overview, navigation to a record list, the record form and a compact window.</li>
 *   <li><b>demo</b> - the portfolio demo walkthrough: the "Coba Demo" button, the
 *       credential-free session, the "Mode Demo" badge, a real record list, and the
 *       reset and exit confirmations. Nothing is typed at any point.</li>
 * </ul>
 *
 * <p>Both flows talk to a backend that is already running on {@code ERP_API_URL}
 * (default {@code http://127.0.0.1:8080}); the harness never boots Quarkus itself, so
 * the screenshots show the same client a reviewer would see.
 */
public final class PortfolioCapture {
    private static Stage stage;
    private static DesktopApp application;
    private static final Path OUT=Path.of("docs","screenshots");
    private static final boolean DEMO="demo".equalsIgnoreCase(System.getenv().getOrDefault("ERP_CAPTURE",""));

    public static void main(String[] args) throws Exception {
        if (DEMO) { captureDemoFlow(); } else { captureNormalFlow(); }
    }

    /** Normal mode: an administrator signs in and walks the workspace. */
    private static void captureNormalFlow() throws Exception {
        String username=System.getenv().getOrDefault("ERP_USER","admin"), password=System.getenv().getOrDefault("ERP_PASSWORD","admin");
        ApiClient api=new ApiClient(System.getenv().getOrDefault("ERP_API_URL","http://127.0.0.1:8080"));
        try { api.login(username,password); } catch (ApiClient.ApiCallException failed) { throw new IllegalStateException(failed.friendlyMessage, failed); }
        Files.createDirectories(OUT); CountDownLatch ready=new CountDownLatch(1); Platform.startup(ready::countDown); ready.await(20,TimeUnit.SECONDS); Platform.setImplicitExit(false);
        try {
            fx(()->{stage=new Stage();application=new DesktopApp();application.start(stage);});
            await(()->root().lookup("#password")!=null);
            // The sign-in button is enabled only after the backend probe finishes;
            // firing it earlier would be swallowed and the capture would hang.
            await(()->!button("Sign in").isDisable());
            fx(()-> {
                capture("sign-in.png");
                button("Sign in").fire();
                if(!hasLabel("Enter both a username and a password."))throw new IllegalStateException("Blank sign-in did not show validation.");
                ((TextField)root().lookup("#username")).setText(username);
                ((PasswordField)root().lookup("#password")).setText(password);
                button("Sign in").fire();
            });
            await(()->hasLabel("Overview"));
            // The workspace fades in over ~160 ms and the dashboard numbers arrive
            // asynchronously, so wait for the metrics before capturing: snapping on the
            // heading alone photographed the fade and produced a blank image.
            await(()->hasLabel("Customers"));
            Thread.sleep(250);
            fx(()->capture("overview.png"));
            fx(()->openEntity("SalInvoice"));
            // An empty tenant is a legitimate state, so accept the empty-state text too.
            await(()->hasLabel(" of ")||hasLabel("No records"));
            fx(()->capture("invoices.png"));
            EntityMeta meta=EntityMeta.parse(api.meta("/api/sales/customers"));
            fx(()-> {
                RecordDialog dialog=new RecordDialog(api,new Catalog.Entry("Customers","/api/sales/customers","SalCustomer"),meta,null,null);
                dialog.initOwner(stage); dialog.show(); dialog.getDialogPane().applyCss(); dialog.getDialogPane().layout();
                try { write(dialog.getDialogPane().snapshot(null,null),OUT.resolve("customer-form.png")); } catch(Exception ex){throw new IllegalStateException(ex);} dialog.close();
            });
            fx(()->{stage.setWidth(1120);stage.setHeight(760);});
            Thread.sleep(400); fx(()->capture("compact-window.png"));
            fx(()->button("Sign out").fire());
            await(()->root().lookup("#password")!=null);
            System.out.println("PASS: real JavaFX sign-in, blank validation, overview, invoice navigation, customer form, compact layout and sign-out. Screenshots: docs/screenshots.");
        } finally { fx(()->{if(application!=null)application.stop();if(stage!=null)stage.close();});Platform.exit(); }
    }

    /**
     * Demo mode: a reviewer walks the whole demo without ever typing a credential.
     *
     * <p>Requires the backend to run with the {@code demo} profile
     * ({@code java -Dquarkus.profile=demo -jar erp-backend/target/quarkus-app/quarkus-run.jar}),
     * so {@code GET /api/demo/status} answers {@code enabled=true} and the sign-in screen
     * offers the button.
     */
    private static void captureDemoFlow() throws Exception {
        Files.createDirectories(OUT); CountDownLatch ready=new CountDownLatch(1); Platform.startup(ready::countDown); ready.await(20,TimeUnit.SECONDS); Platform.setImplicitExit(false);
        try {
            fx(()->{stage=new Stage();application=new DesktopApp();application.start(stage);});
            await(()->root().lookup("#password")!=null);
            // The demo button is revealed only once the backend confirms demo mode is on.
            await(()->{Button demo=buttonOrNull("Coba Demo");return demo!=null&&demo.isVisible()&&!demo.isDisable();});
            fx(()->capture("demo-sign-in.png"));

            // No username, no password: the button mints the session. The first call also
            // seeds the demo tenant, which is allowed a generous window.
            fx(()->button("Coba Demo").fire());
            await(()->hasLabel("Overview"),240);
            await(()->hasLabel("Demo workspace"));
            // The dashboard numbers arrive asynchronously; capturing on the heading alone
            // would photograph an empty overview.
            await(()->hasLabel("Customers"),240);
            await(()->hasLabel("Recent invoices"),240);
            await(PortfolioCapture::animationsFinished);
            fx(()->capture("demo-overview.png"));

            fx(()->openEntity("SalInvoice"));
            await(()->hasLabel(" of "));
            await(PortfolioCapture::animationsFinished);
            fx(()->capture("demo-invoices.png"));

            fx(()->openEntity("SalCustomer"));
            await(()->hasLabel(" of "));
            await(PortfolioCapture::animationsFinished);
            fx(()->capture("demo-customers.png"));

            // The destructive actions are behind modal confirmations. Opening one from a
            // blocking fx() call would wait for the dialog to close, so it is fired
            // without blocking, snapshotted, and then dismissed.
            Platform.runLater(()->button("Reset Data Demo").fire());
            snapshotOpenDialog("demo-reset-confirm.png");
            fx(()->dismissOpenDialog());

            // Leaving the demo is confirmed, then the shell returns to the sign-in screen.
            Platform.runLater(()->button("Keluar Demo").fire());
            snapshotOpenDialog("demo-exit-confirm.png");
            fx(()->acceptOpenDialog());
            await(()->root().lookup("#password")!=null,60);
            fx(()->capture("demo-exited.png"));

            System.out.println("PASS: demo sign-in without credentials, Mode Demo badge, record lists, reset and exit confirmations, and the return to sign-in. Screenshots: docs/screenshots.");
        } finally { fx(()->{if(application!=null)application.stop();if(stage!=null)stage.close();});Platform.exit(); }
    }

    private static Parent root(){return stage.getScene().getRoot();}
    private static boolean animationsFinished() {
        var holder = root().lookup("#page-holder");
        return root().getOpacity() >= 0.99 && (!(holder instanceof javafx.scene.layout.VBox box)
                || box.getChildren().isEmpty() || box.getChildren().getFirst().getOpacity() >= 0.99);
    }
    private static boolean hasLabel(String text){return root().lookupAll(".label").stream().filter(Label.class::isInstance).map(Label.class::cast).anyMatch(l->l.getText()!=null&&l.getText().contains(text));}
    private static Button button(String text){return root().lookupAll(".button").stream().filter(Button.class::isInstance).map(Button.class::cast).filter(b->b.getText().equals(text)).findFirst().orElseThrow();}
    private static Button buttonOrNull(String text){return root().lookupAll(".button").stream().filter(Button.class::isInstance).map(Button.class::cast).filter(b->b.getText().equals(text)).findFirst().orElse(null);}

    @SuppressWarnings("unchecked") private static void openEntity(String entity){
        var entry=com.erp.platform.ModuleRegistry.ENTRIES.stream().filter(e->e.entity().equals(entity)).findFirst().orElseThrow();
        // MainWindow builds the tree with Object nodes (division labels are Strings, leaves
        // are EntryItem records), so the lookup has to be typed the same way.
        TreeView<Object> nav=(TreeView<Object>)root().lookup("#module-tree");
        var branch=nav.getRoot().getChildren().stream().filter(b->b.getValue().equals(entry.divisionLabel())).findFirst().orElseThrow();
        var leaf=branch.getChildren().stream().filter(b->String.valueOf(b.getValue()).equals(entry.label())).findFirst().orElseThrow();
        branch.setExpanded(true); nav.getSelectionModel().select(leaf);
    }

    private static void fx(Runnable action) throws Exception { FutureTask<Void> task=new FutureTask<>(action,null);Platform.runLater(task);task.get(60,TimeUnit.SECONDS); }

    private static void await(BooleanSupplier predicate) throws Exception { await(predicate,90); }

    private static void await(BooleanSupplier predicate,int seconds) throws Exception {
        long deadline=System.nanoTime()+TimeUnit.SECONDS.toNanos(seconds);
        while(System.nanoTime()<deadline){FutureTask<Boolean> check=new FutureTask<>(predicate::getAsBoolean);Platform.runLater(check);if(check.get(10,TimeUnit.SECONDS))return;Thread.sleep(150);}
        throw new IllegalStateException("The view did not become ready.");
    }

    /** Snapshots the modal dialog that is currently showing, if any. */
    private static void snapshotOpenDialog(String name) throws Exception {
        long deadline=System.nanoTime()+TimeUnit.SECONDS.toNanos(30);
        DialogPane pane=null;
        while(System.nanoTime()<deadline){
            FutureTask<DialogPane> find=new FutureTask<>(()->Window.getWindows().stream()
                    .filter(w->w.isShowing()&&w.getScene()!=null&&w.getScene().getRoot() instanceof DialogPane)
                    .map(w->(DialogPane)w.getScene().getRoot()).findFirst().orElse(null));
            Platform.runLater(find);
            pane=find.get(10,TimeUnit.SECONDS);
            if(pane!=null)break;
            Thread.sleep(120);
        }
        if(pane==null)throw new IllegalStateException("No confirmation dialog appeared for "+name);
        final DialogPane target=pane;
        // The dialog has only just been shown; without letting its layout settle the
        // snapshot catches the buttons at their initial, un-sized width and the action
        // labels come out elided.
        Thread.sleep(350);
        fx(()->{target.applyCss();target.layout();});
        Thread.sleep(150);
        fx(()->{target.applyCss();target.layout();});
        FutureTask<WritableImage> shot=new FutureTask<>(()->target.snapshot(null,null));
        Platform.runLater(shot);
        write(shot.get(30,TimeUnit.SECONDS),OUT.resolve(name));
    }

    /** Presses Cancel on the open confirmation dialog so the flow continues. */
    private static void dismissOpenDialog(){
        fireDialogButton(ButtonBar.ButtonData.CANCEL_CLOSE);
    }

    /** Accepts the open confirmation dialog, letting the guarded action proceed. */
    private static void acceptOpenDialog(){
        fireDialogButton(ButtonBar.ButtonData.OK_DONE);
    }

    private static void fireDialogButton(ButtonBar.ButtonData data){
        Window.getWindows().stream()
                .filter(w->w.isShowing()&&w.getScene()!=null&&w.getScene().getRoot() instanceof DialogPane)
                .findFirst().ifPresent(w->{
                    DialogPane pane=(DialogPane)w.getScene().getRoot();
                    pane.getButtonTypes().stream()
                            .filter(bt->bt.getButtonData()==data)
                            .findFirst().map(pane::lookupButton)
                            .filter(Button.class::isInstance).map(Button.class::cast)
                            .ifPresent(Button::fire);
                });
    }

    private static void capture(String name) { try { root().applyCss();root().layout();write(root().snapshot(null,null),OUT.resolve(name)); }catch(Exception ex){throw new IllegalStateException(ex);} }
    private static void write(WritableImage source,Path target)throws Exception{
        int width=(int)source.getWidth(),height=(int)source.getHeight();BufferedImage output=new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
        for(int y=0;y<height;y++)for(int x=0;x<width;x++)output.setRGB(x,y,source.getPixelReader().getArgb(x,y));ImageIO.write(output,"png",target.toFile());
    }
}
