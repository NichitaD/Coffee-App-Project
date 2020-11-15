package com.myprojects.corso.viewModels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.myprojects.corso.model.CoffeeShop;
import com.myprojects.corso.model.Offer;
import com.myprojects.corso.repositories.FirebaseRepository;

import java.util.ArrayList;

public class ShopOwnerActivityViewModel extends ViewModel {

    private FirebaseRepository mRepo;
    private MutableLiveData<CoffeeShop> mCoffeeShop;

    public void init(String email) {
        if(mCoffeeShop != null) {
            return;
        }
        mRepo = FirebaseRepository.getInstance();
        mCoffeeShop = mRepo.getCoffeeShopByUser(email);
    }

    public LiveData<CoffeeShop> getCoffeeShop(){
        return mCoffeeShop;
    }

    public void deleteOffer (int position) {
        Offer offerToRemove = new Offer();
        offerToRemove.setText(mCoffeeShop.getValue().getName());
        offerToRemove.setText(mCoffeeShop.getValue().getOffers().get(position));
        mRepo.deleteOffer(offerToRemove, () -> {});
    }

    public void postOffer (String text) {
        Offer offertoPost = new Offer() ;
        offertoPost.setText(text);
        offertoPost.setCoffeeShop(mCoffeeShop.getValue().getName());
        mRepo.postOffer(offertoPost, () -> {});
    }
}
