package com.myprojects.corso.repositories;

import android.net.Uri;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.annotations.Nullable;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.myprojects.corso.R;
import com.myprojects.corso.model.CoffeeShop;
import com.myprojects.corso.model.Offer;
import com.myprojects.corso.model.Review;
import com.myprojects.corso.model.User;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class FirebaseRepository {

    private static  FirebaseRepository instance;
    private FirebaseFirestore database;
    private FirebaseAuth mAuth;
    private MutableLiveData<ArrayList<CoffeeShop>> mCoffeeShops = new MutableLiveData<>();
    private MutableLiveData<CoffeeShop> mSingleCoffeeShop = new MutableLiveData<>();
    private MutableLiveData<CoffeeShop> mAdminCoffeeShop = new MutableLiveData<>();
    private MutableLiveData<User> mUser = new MutableLiveData<>();
    private MutableLiveData <ArrayList<Offer>> mOffers = new MutableLiveData<>();

    public FirebaseRepository() {
        database = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        addOffersListener();
    }

    public static FirebaseRepository getInstance() {
        if(instance == null) {
            instance = new FirebaseRepository();
        }
        return instance;
    }

    public MutableLiveData<ArrayList<CoffeeShop>> getCoffeeShops(){
        setCoffeeShops(() -> {});
        return mCoffeeShops;
    }

    public MutableLiveData<CoffeeShop> getOneCoffeeShop(String name) {
        setCoffeeShops(() -> {
            for(CoffeeShop coffeeShop : mCoffeeShops.getValue()) {
                if (coffeeShop.getName().equals(name)) {
                    mSingleCoffeeShop.setValue(coffeeShop);
                    break;
                }
            }
        });
        return mSingleCoffeeShop;
    }

    public MutableLiveData<CoffeeShop> getCoffeeShopByUser(String email) {
        DocumentReference docRef = database.collection("users").document(email);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        mAdminCoffeeShop.setValue(getOneCoffeeShop((String) document.get("name")).getValue());
                    } else {
                        // TODO: ADD stuff
                    }
                } else {
                    // TODO: ADD stuff
                }
            }
        });
        return mAdminCoffeeShop;
    }

    public MutableLiveData<User> getUser() {
        setUser();
        return mUser;
    }

    public MutableLiveData<ArrayList<Offer>> getOffers() {
        setOffers();
        return mOffers;
    }

    // Gets the user info from the database
    public void setUser() {
        User user = new User().getUser();
        DocumentReference docRef = database.collection("users").document(mAuth.getCurrentUser().getEmail());
        docRef.get().addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    user.setEmail(mAuth.getCurrentUser().getEmail());
                    user.setGrantedAccess((boolean) document.get("access"));
                    user.setCoffeeShopAdmin((boolean) document.get("coffee_shop"));
                    if(!user.isCoffeeShopAdmin()) {
                        user.setWeekleyCoffees((HashMap<String, Long>) document.get("weekley_coffees"));
                        user.setLastAccessDate((Date) document.getTimestamp("last_access_date").toDate());
                        user.setTodaysCoffees((Long) document.get("today"));
                    }
                    mUser.setValue(user);
                }
            }
        });
    }

    // Gets the coffee shops info from the database
    public void setCoffeeShops(Runnable callback) {
        CollectionReference coffeeShopsRef = database.collection("coffee_shops");
        coffeeShopsRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>(){
            @Override
            public void onComplete(@NonNull Task< QuerySnapshot > task) {
                if (task.isSuccessful()) {
                    ArrayList<CoffeeShop> coffeeShops = new ArrayList<>();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        CoffeeShop coffeeShop = new CoffeeShop();
                        coffeeShop.setName((String) document.get("name"));
                        coffeeShop.setLocation((GeoPoint) document.get("location"));
                        coffeeShop.setOffers((ArrayList<String>) document.get("offers"));
                        coffeeShop.setRating((Long) document.get("rating"));
                        coffeeShop.setReviews((ArrayList<HashMap<String,String>>) document.get("reviews"));
                        coffeeShop.setLogoUri((String) document.get("logoUri"));
                        coffeeShops.add(coffeeShop);
                    }
                    mCoffeeShops.setValue(coffeeShops);
                    callback.run();
                } else {
                    Log.d("Database_info", "Error getting documents: ", task.getException());
                }
            }
        });
    }

    // Gets the offers from the database
    public void setOffers() {
        ArrayList<Offer> offers = new ArrayList<>();
        DocumentReference doc_ref = database.collection("offers").document("offers");
        doc_ref.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        ArrayList<Map> mapArray = (ArrayList<Map>) document.get("offers");
                        for (Map<String, Map<String, String>> map : mapArray) {
                            for (Map.Entry<String, Map<String, String>> entry : map.entrySet()) {
                                for (Map.Entry<String, String> second_entry : entry.getValue().entrySet()) {
                                    Offer offer = new Offer();
                                    offer.setCoffeeShop(second_entry.getKey());
                                    offer.setText(second_entry.getValue());
                                    offers.add(offer);
                                }
                            }
                        }
                        mOffers.setValue(offers);
                    } else {
                        //TODO Add stuff
                    }
                } else {
                    //TODO Add stuff
                }
        });
    }

    public void postOffer(Offer offer, Runnable callback) {
        final Map<String, Object> addOfferToArrayMap = new HashMap<>();
        addOfferToArrayMap.put("offers", FieldValue.arrayUnion(offer));
        database.collection("coffee_shops").document(offer.getCoffeeShop().toLowerCase().
                replaceAll("[^A-Za-z0-9']", "_")).update(addOfferToArrayMap);
        DocumentReference doc_ref = database.collection("offers").document("offers");
        doc_ref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Long iterator =(Long) document.get("iterator");
                        Map <String, String> map1 = new HashMap<>();
                        map1.put(offer.getCoffeeShop(),offer.getText());
                        Map <String, Map> map2 = new HashMap<>();
                        map2.put("offer"+iterator, map1);
                        Map <String, Object> addOffer = new HashMap<>();
                        addOffer.put("offers",FieldValue.arrayUnion(map2));
                        addOffer.put("iterator", FieldValue.increment(1));
                        database.collection("offers").document("offers").update(addOffer);
                        setOffers();
                        callback.run();
                    } else {

                    }
                } else {

                }
            }
        });
    }

    public void deleteOffer (Offer offer, Runnable callback) {
        // Delete it from the coffeeShops array
        final Map<String, Object> removeOfferFromArrayMap = new HashMap<>();
        removeOfferFromArrayMap.put("offers", FieldValue.arrayRemove(offer.getText()));
        database.collection("coffee_shops").document(offer.getCoffeeShop().toLowerCase().
                replaceAll("[^A-Za-z0-9']", "_")).update(removeOfferFromArrayMap);

        // Delete it from the general offers array

        DocumentReference doc_ref = database.collection("offers").document("offers");
        doc_ref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()){
                        ArrayList<Map> offer_array = (ArrayList<Map>) document.get("offers");
                        for(Map <String, Map<String,String>> map: offer_array){
                            Log.d("Remover", "onComplete: started going through array" + map);
                            for (Map.Entry<String, Map<String, String>> entry : map.entrySet()){
                                Log.d("Remover", "onComplete: opened map" + entry);
                                for (Map.Entry<String, String> second_entry : entry.getValue().entrySet()) {
                                    if (offer.getCoffeeShop().equals(second_entry.getKey()) && second_entry.getValue().equals(offer.getText())){
                                        Log.d("Remover", "onComplete: found offer to delete");
                                        Map<String, Object> removeMe = new HashMap<>();
                                        removeMe.put("offers", FieldValue.arrayRemove(map));
                                        database.collection("offers").document("offers").update(removeMe);
                                        return;
                                    }
                                }
                            }
                        }
                        setOffers();
                        callback.run();
                    } else {

                    }
                } else {

                }
            }
        });

    }

    public void postReview(String coffeShopId, Review review, Runnable callback) {
        DocumentReference doc_ref = database.collection("coffee_shops").document(coffeShopId);
        doc_ref.update("ratings_sum", FieldValue.increment(review.getRating()));
        doc_ref.update("number_of_ratings", FieldValue.increment(1));
        database.runTransaction(new Transaction.Function<Void>() {
            @Override
            public Void apply(Transaction transaction) throws FirebaseFirestoreException {
                DocumentSnapshot snapshot = transaction.get(doc_ref);
                ArrayList<HashMap<String, String>> reviews = (ArrayList<HashMap<String, String>>)  snapshot.get("reviews");
                Double update_rating = (Double) snapshot.get("ratings_sum") / (Long) snapshot.get("number_of_ratings");
                HashMap<String, String> newReview = new HashMap<>();
                newReview.put("author", review.getAuthor());
                newReview.put("rating", review.getRating().toString());
                newReview.put("text", review.getText());
                reviews.add(newReview);
                transaction.update(doc_ref, "reviews", reviews);
                transaction.update(doc_ref, "rating", update_rating.longValue());
                setCoffeeShops(() -> {});
                return null;
            }
        }).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                setUser();
                callback.run();
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("FirebaseRepo", "Transaction failure.", e);
                    }
                });
    }

    public void incrementCoffeeCount() {
        DocumentReference docRef = database.collection("users").document(mAuth.getCurrentUser().getEmail());
        docRef.update("today", FieldValue.increment(1));
        setUser();
    }

    public void decrementCoffeeCount() {
        DocumentReference docRef = database.collection("users").document(mAuth.getCurrentUser().getEmail());
        docRef.update("today", FieldValue.increment(-1));
        setUser();
    }

    public void resetLastAccessDay() {
        DocumentReference doc_date = database.collection("users").document(mAuth.getCurrentUser().getEmail());
        doc_date.update("last_access_date", new Timestamp(Calendar.getInstance().getTime()));
        doc_date.update("today", 0);
    }

    public void updateCoffeeCount(String day, Integer numberOfCoffees) {
        DocumentReference docRef = database.collection("users").document(mAuth.getCurrentUser().getEmail());
        HashMap<String, Integer> dayUpdate = mUser.getValue().getWeekleyCoffees();
        dayUpdate.replace(day, numberOfCoffees);
        docRef.update("weekley_coffees", dayUpdate);
    }

    public void resetLastAccessWeek() {
        DocumentReference doc_date = database.collection("users").document(mAuth.getCurrentUser().getEmail());
        HashMap<String, Integer> weekleyCoffees = new HashMap<>();
        weekleyCoffees.put("Monday", 0);
        weekleyCoffees.put("Tuesday", 0);
        weekleyCoffees.put("Wednesday", 0);
        weekleyCoffees.put("Thursday", 0);
        weekleyCoffees.put("Friday", 0);
        weekleyCoffees.put("Saturday", 0);
        weekleyCoffees.put("Sunday", 0);

        doc_date.update("weekley_coffees", weekleyCoffees);
        setUser();
    }

    public void addOffersListener() {
                final DocumentReference docRef = database.collection("offers").document("offers");
        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    return;
                } else {
                    setOffers();
                }
            }
        });
    }

}
