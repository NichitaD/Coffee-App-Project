package com.myprojects.corso.viewModels;

import android.location.Location;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.firestore.GeoPoint;
import com.myprojects.corso.model.CoffeeShop;
import com.myprojects.corso.repositories.FirebaseRepository;

import java.util.ArrayList;

public class MapsActivityViewModel extends ViewModel {

    private MutableLiveData<ArrayList<CoffeeShop>> mCoffeeShops;
    private FirebaseRepository mRepo;

    public void init() {
        if(mCoffeeShops != null) {
            return;
        }
        mRepo = FirebaseRepository.getInstance();
        mCoffeeShops = mRepo.getCoffeeShops();
    }

    public LiveData<ArrayList<CoffeeShop>> getCoffeeShops() {
        return mCoffeeShops;
    }

    public CoffeeShop getNearestCoffeeShop(Location myLocation) {
        GeoPoint shopGeopoint;
        Location shopLocation = new Location("");
        Float testDistance;
        Float closestDistance = new Float(123456789);
        CoffeeShop nearestCoffeeShop = new CoffeeShop();
        for(CoffeeShop coffeeShop : mCoffeeShops.getValue()) {
            shopGeopoint = coffeeShop.getLocation();
            shopLocation.setLatitude(shopGeopoint.getLatitude());
            shopLocation.setLongitude(shopGeopoint.getLongitude());
            testDistance = myLocation.distanceTo(shopLocation);
            if (testDistance < closestDistance) {
                closestDistance = testDistance;
                nearestCoffeeShop = coffeeShop;
            }
        }
        return nearestCoffeeShop;
    }
}
