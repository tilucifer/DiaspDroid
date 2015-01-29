package fr.android.scaron.diaspdroid.vues.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Response;

import org.acra.ACRA;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import fr.android.scaron.diaspdroid.R;
import fr.android.scaron.diaspdroid.activity.DiaspActivity;
import fr.android.scaron.diaspdroid.controler.CookieControler;
import fr.android.scaron.diaspdroid.controler.DataControler;
import fr.android.scaron.diaspdroid.controler.DiasporaControler;
import fr.android.scaron.diaspdroid.controler.LogControler;
import fr.android.scaron.diaspdroid.controler.ProfilControler;
import fr.android.scaron.diaspdroid.model.Post;
import fr.android.scaron.diaspdroid.vues.adapter.PostAdapter;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnFragmentInteractionListener}
 * interface.
 */
public class FluxFragment extends Fragment implements AbsListView.OnItemClickListener {

    private static Logger LOGGEUR = LoggerFactory.getLogger(FluxFragment.class);
    private static LogControler LOG = LogControler.getInstance(LOGGEUR);
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";

    // TODO: Rename and change types of parameters
    private String mTitle;
    private ActionBarActivity mActivity;

    private OnFragmentInteractionListener mListener;

    /**
     * The fragment's ListView/GridView.
     */
    private AbsListView mListView;

    /**
     * The Adapter which will be used to populate the ListView/GridView with
     * Views.
     */
    private PostAdapter mAdapter;

    // TODO: Rename and change types of parameters
    public static FluxFragment newInstance(String title, ActionBarActivity activity, int sectionNumber) {
        try{
            FluxFragment fragment = new FluxFragment();
            fragment.setmTitle(title);
            fragment.setmActivity(activity);
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        } catch (Throwable thr) {
            LOG.e("Erreur : " + thr.toString());
            ACRA.getErrorReporter().handleException(thr);
            throw thr;
        }
    }
    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public FluxFragment() {
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        try{
            super.onCreate(savedInstanceState);

            //Callback de la récupération du flux
            final FutureCallback<List<Post>> fluxCallback = new FutureCallback<List<Post>>() {
                @Override
                public void onCompleted(Exception e, List<Post> posts) {

                    LOG.d(FluxFragment.class, "Callback flux, exception ? " + e);
                    if (e!=null && e.getCause()!=null) {
                        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getmActivity());
                        final AlertDialog alertDialog = alertDialogBuilder.create();
                        alertDialog.setIcon(R.drawable.ic_launcher);
                        alertDialog.setTitle("PB Données");
                        alertDialog.setMessage("La récupétion de votre flux a échouée");
                        alertDialog.show();
                        LOG.d(FluxFragment.class , "Callback flux, cause exception ? " + e.getCause().getMessage());
                    }
                    if (posts!=null){
                        mAdapter.setPosts(posts);
                        return;
                    }
                    LOG.e("Callback flux, Erreur : " + e.toString());
                    if (e.getCause()!=null) {
                        LOG.e("Callback flux, cause exception ? " + e.getCause().getMessage());
                    }
                    ACRA.getErrorReporter().handleException(e);
                }
            };

//            //Callback d'envoi du formulaire de login
//            final FutureCallback<Response<String>> loginCallback = new FutureCallback<Response<String>>() {
//                @Override
//                public void onCompleted(Exception e, Response<String> result) {
//                    boolean resultOK = ProfilControler.onCompleteLogin(e, result);
//                    if (!resultOK){
//                        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getmActivity());
//                        final AlertDialog alertDialog = alertDialogBuilder.create();
//                        alertDialog.setIcon(R.drawable.ic_launcher);
//                        alertDialog.setTitle("PB Connexion");
//                        alertDialog.setMessage("La connexion a Diaspora a échouée");
//                        alertDialog.show();
//                        return;
//                    }
//                    //On est loggué donc on demande le flux
//                    DataControler.getStream(getmActivity().getApplicationContext(), fluxCallback);
//                }
//            };

//            //Callback de récupération du formulaire de login et d'accès au authenticity_token
//            final FutureCallback<Response<String>> tokenCallback = new FutureCallback<Response<String>>() {
//                @Override
//                public void onCompleted(Exception e, Response<String> result) {
//                    boolean resultOK = ProfilControler.onCompleteGetToken(e, result);
//                    if (!resultOK){
//                        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getmActivity());
//                        final AlertDialog alertDialog = alertDialogBuilder.create();
//                        alertDialog.setIcon(R.drawable.ic_launcher);
//                        alertDialog.setTitle("PB Accès");
//                        alertDialog.setMessage("L'accès a Diaspora est impossible");
//                        alertDialog.show();
//                        return;
//                    }
//                    //On a le token donc on demande le login
//                    ProfilControler.login(getmActivity(), "tilucifer", "Pikifou01", loginCallback);
//                }
//            };

            CookieControler cookieControler = CookieControler.getInstance(getmActivity());
            cookieControler.clearCookies();
//            ProfilControler.getToken(getmActivity(), tokenCallback);

            DiasporaControler.getStreamFlow(getmActivity(), fluxCallback);

            mAdapter = new PostAdapter(getActivity(), R.layout.fragment_flux_list, new ArrayList<Post>());
        } catch (Throwable thr) {
            LOG.e("Erreur : " + thr.toString());
            ACRA.getErrorReporter().handleException(thr);
            throw thr;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        try{
            View view = inflater.inflate(R.layout.fragment_flux, container, false);

            // Set the adapter
            mListView = (AbsListView) view.findViewById(R.id.fragment_flux_list);
            ((AdapterView<ListAdapter>) mListView).setAdapter(mAdapter);

            // Set OnItemClickListener so we can be notified on item clicks
            mListView.setOnItemClickListener(this);

            return view;
        } catch (Throwable thr) {
            LOG.e("Erreur : " + thr.toString());
            ACRA.getErrorReporter().handleException(thr);
            throw thr;
        }
    }

    @Override
    public void onAttach(Activity activity) {
        try{
            super.onAttach(activity);
            ((DiaspActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
            mListener = (OnFragmentInteractionListener) activity;
        } catch (Throwable thr) {
            LOG.e("Erreur : " + thr.toString());
            ACRA.getErrorReporter().handleException(thr);
            throw thr;
        }

    }

    @Override
    public void onDetach() {
        try{
            super.onDetach();
            mListener = null;
        } catch (Throwable thr) {
            LOG.e("Erreur : " + thr.toString());
            ACRA.getErrorReporter().handleException(thr);
            throw thr;
        }
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        try{
            if (null != mListener) {
                // Notify the active callbacks interface (the activity, if the
                // fragment is attached to one) that an item has been selected.
                mListener.onFragmentInteraction(FluxContent.ITEMS.get(position).getIdStr());
            }
        } catch (Throwable thr) {
            LOG.e("Erreur : " + thr.toString());
            ACRA.getErrorReporter().handleException(thr);
            throw thr;
        }
    }

    /**
     * The default content for this Fragment has a TextView that is shown when
     * the list is empty. If you would like to change the text, call this method
     * to supply the text it should use.
     */
    public void setEmptyText(CharSequence emptyText) {
        try{
            View emptyView = mListView.getEmptyView();

            if (emptyView instanceof TextView) {
                ((TextView) emptyView).setText(emptyText);
            }
        } catch (Throwable thr) {
            LOG.e("Erreur : " + thr.toString());
            ACRA.getErrorReporter().handleException(thr);
            throw thr;
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(String id);
    }


    public String getmTitle() {
        return mTitle;
    }

    public void setmTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public ActionBarActivity getmActivity() {
        return mActivity;
    }

    public void setmActivity(ActionBarActivity mActivity) {
        this.mActivity = mActivity;
    }
}
