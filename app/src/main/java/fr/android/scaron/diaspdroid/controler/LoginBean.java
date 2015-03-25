package fr.android.scaron.diaspdroid.controler;

import android.content.Context;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.rest.RestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import fr.android.scaron.diaspdroid.model.DiasporaConfig;
import fr.android.scaron.diaspdroid.model.Post;

/**
 * Created by Sébastien on 11/03/2015.
 */
@EBean
public class LoginBean {
    private static Logger LOGGEUR = LoggerFactory.getLogger(LoginBean.class);
    private static LogControler LOG = LogControler.getLoggeur(LOGGEUR);
    private static String TAG = "LoginBean";
    @RootContext
    Context context;

    @RestService
    LoginService loginService;
    @Bean
    ErrorHandlerBean errorHandlerBean;

    @AfterInject
    public void init(){
        loginService.setRestErrorHandler(errorHandlerBean);
        if (DiasporaControler.COOKIE_SESSION_STREAM!=null && !DiasporaControler.COOKIE_SESSION_STREAM.isEmpty()) {
            loginService.setCookie("_diaspora_session", DiasporaControler.COOKIE_SESSION_STREAM);
        }else if (DiasporaControler.COOKIE_SESSION_LOGIN!=null && !DiasporaControler.COOKIE_SESSION_LOGIN.isEmpty()) {
            loginService.setCookie("_diaspora_session", DiasporaControler.COOKIE_SESSION_LOGIN);
        } else if (DiasporaControler.COOKIE_SESSION_TOKEN!=null && !DiasporaControler.COOKIE_SESSION_TOKEN.isEmpty()) {
            loginService.setCookie("_diaspora_session", DiasporaControler.COOKIE_SESSION_TOKEN);
        }
        if (DiasporaControler.TOKEN!=null && !DiasporaControler.TOKEN.isEmpty()){
            loginService.setHeader("x-csrf-token", DiasporaControler.TOKEN);
        }
    }

    public boolean seLogguer(){
        String TAG_METHOD = TAG + ".seLogguer : ";
        LOG.d(TAG_METHOD+ "Entrée");
        boolean resultOK = true;
        boolean resultKO = false;
        loginService.setRootUrl(DiasporaConfig.POD_URL);
        boolean getTokenOK = getToken(loginService.getLoginHTML());
        LOG.d(TAG_METHOD+ "Token crsf obtenu ? "+getTokenOK);
        if (!getTokenOK){
            LOG.d(TAG_METHOD+ "Sortie en erreur");
            return resultKO;
        }
//        loginService.setHeader("x-csrf-token", DiasporaControler.TOKEN);
        DiasporaControler.COOKIE_SESSION_TOKEN = loginService.getCookie("_diaspora_session");
        if (DiasporaControler.COOKIE_SESSION_TOKEN!=null && !DiasporaControler.COOKIE_SESSION_TOKEN.isEmpty()) {
            loginService.setCookie("_diaspora_session", DiasporaControler.COOKIE_SESSION_TOKEN);
        }
        loginService.setRootUrl(DiasporaConfig.POD_URL);
        String loggued = loginService.postLogin();

        LOG.d(TAG_METHOD+ "Login obtenu ? "+loggued);
        LOG.d(TAG_METHOD+ "Sortie");
        return resultOK;
    }

    public List<Post> getStream(){
        boolean logged = seLogguer();
        if (logged){
            return loginService.getStream();//streamPostsService.getStream();
        }
        List<Post> emptyError =  new ArrayList<Post>();
        Post empty = new Post();
        empty.setText("Aucune réponse n'a été trouvée");
        emptyError.add(empty);
        return emptyError;
    }

    public boolean getToken(String response){
        String TAG_METHOD = TAG + ".getToken : ";
        LOG.d(TAG_METHOD+ "Entrée");
        boolean resultOK = true;
        boolean resultKO = false;
        if (response==null || response.isEmpty()){
            return resultKO;
        }

        int indexTokenName = response.indexOf("<meta content=\"authenticity_token\" name=\"csrf-param\" />",0);
        if (indexTokenName<=0) {
            LOG.d(TAG_METHOD+ "\t**\tIMPOSSIBLE de trouver le token");
            LOG.d(TAG_METHOD+ "Sortie");
            return resultKO;
        }
        int indexToken = response.indexOf("<meta content=\"", indexTokenName + 1);
        LOG.d(TAG_METHOD+ "**	token found in "+response.substring(indexToken, response.indexOf("/>", indexToken)));
        indexToken = indexToken+"<meta content=\"".length();
        int indexEndToken = response.indexOf("\" name=\"csrf-token\"", indexToken+1);
        if (DiasporaControler.TOKEN.isEmpty()) {
            DiasporaControler.TOKEN = response.substring(indexToken, indexEndToken);
            LOG.d(TAG_METHOD+ "\t**\ttoken récolté '" + DiasporaControler.TOKEN + "'");
        }else{
            LOG.d(TAG_METHOD+ "\t**\ttoken déjà récolté '" + DiasporaControler.TOKEN + "'");
        }
        LOG.d(TAG_METHOD+ "Sortie");
        return resultOK;
    }

}