package com.myprojects.corso.viewModels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.myprojects.corso.model.CoffeeShop;
import com.myprojects.corso.model.Offer;
import com.myprojects.corso.model.Review;
import com.myprojects.corso.repositories.FirebaseRepository;

import java.util.ArrayList;

public class ReviewsActivityViewModel extends ViewModel {
    private MutableLiveData<CoffeeShop> mCoffeeShop;
    private FirebaseRepository mRepo;

    public void init(String name) {
        mRepo = FirebaseRepository.getInstance();
        mCoffeeShop = mRepo.getOneCoffeeShop(name);
    }

    public LiveData<CoffeeShop> getCoffeeShop() {
        return mCoffeeShop;
    }
}
