package com.myprojects.corso.viewModels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.myprojects.corso.model.CoffeeShop;
import com.myprojects.corso.model.Offer;
import com.myprojects.corso.repositories.FirebaseRepository;

import java.util.ArrayList;

public class OffersActivityViewModel extends ViewModel {
    private MutableLiveData<ArrayList<Offer>> mOffers;
    private FirebaseRepository mRepo;

    public void init() {
        mRepo = FirebaseRepository.getInstance();
        mOffers = mRepo.getOffers();
    }

    public LiveData<ArrayList<Offer>> getOffers() {
        return mOffers;
    }
}
