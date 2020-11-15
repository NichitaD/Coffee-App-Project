package com.myprojects.corso.viewModels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.myprojects.corso.model.CoffeeShop;
import com.myprojects.corso.model.Review;
import com.myprojects.corso.repositories.FirebaseRepository;


public class CoffeeShopActivityViewModel extends ViewModel {
    private MutableLiveData<CoffeeShop> mCoffeeShop;
    private FirebaseRepository mRepo;

    public void init(String name) {
        if(mCoffeeShop != null) {
            return;
        }
        mRepo = FirebaseRepository.getInstance();
        mCoffeeShop = mRepo.getOneCoffeeShop(name);
    }

    public LiveData<CoffeeShop> getCoffeeShop() {
        return mCoffeeShop;
    }

    public void addReview(Review review) {
        String coffee_shop_id = mCoffeeShop.getValue().getName().toLowerCase().replaceAll("[^A-Za-z0-9']", "_");
        mRepo.postReview(coffee_shop_id, review, () -> {});
    }
}
