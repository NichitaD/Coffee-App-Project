package com.myprojects.corso.viewModels;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.myprojects.corso.model.CoffeeShop;
import com.myprojects.corso.model.User;
import com.myprojects.corso.repositories.FirebaseRepository;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class MenuActivityViewModel extends ViewModel {
    private FirebaseRepository mRepo;
    private MutableLiveData<ArrayList<CoffeeShop>> mCoffeeShops;
    private MutableLiveData<User> user;
    private MutableLiveData<Integer> coffeeCount;

    public void init() {
        if(mCoffeeShops != null) {
            return;
        }
        mRepo = FirebaseRepository.getInstance();
        mCoffeeShops = mRepo.getCoffeeShops();
        user = mRepo.getUser();
    }

    public LiveData<User> getUser() { return user; };

    public String getDayName(Integer day){
        String day_name = new String();
        switch (day) {
            case 2 : day_name = "Monday"; break;
            case 3 : day_name = "Tuesday"; break;
            case 4 : day_name = "Wednesday"; break;
            case 5 : day_name = "Thursday"; break;
            case 6 : day_name = "Friday"; break;
            case 7 : day_name = "Saturday"; break;
            case 1 : day_name = "Sunday"; break;
        }
        return day_name;
    }

    public void incrementCount() {
        mRepo.incrementCoffeeCount();
    }

    public void decrementCount() {
        mRepo.decrementCoffeeCount();
    }

    public ArrayList<String> getCoffeeShopNames() {
        ArrayList<String> nameCollection = new ArrayList<>();
         mRepo.getCoffeeShops().getValue().forEach(coffeeShop -> {
             nameCollection.add(coffeeShop.getName());
         });
        return nameCollection;
    }

    public void checkDate() {
        if(this.user.getValue() == null || this.user.getValue().getEmail() == null) {
            return;
        }
        Date last_access_date = user.getValue().getLastAccessDate();
        Log.d("Date: ", last_access_date.toString());
        Calendar current_date = Calendar.getInstance();
        Calendar oldDate = Calendar.getInstance();
        oldDate.setTime(last_access_date);
        Integer this_day = current_date.get(Calendar.DAY_OF_MONTH);
        Integer old_day = oldDate.get(Calendar.DAY_OF_MONTH);
        Integer this_week = current_date.get(Calendar.WEEK_OF_YEAR);
        Integer old_week = oldDate.get(Calendar.WEEK_OF_YEAR);
        // TODO: Fomd a better expression
        if (old_day - this_day < 0 || this_day - old_day < 0) {
            mRepo.resetLastAccessDay();
        }
        if (old_week - this_week < 0 || this_week - old_week < 0) {
            mRepo.resetLastAccessWeek();
        }
        updateDatabase();
    }

    public void updateDatabase(){
        Calendar current_date = Calendar.getInstance();
        Calendar last_acces =  Calendar.getInstance();
        last_acces.setTime(user.getValue().getLastAccessDate());
        String day_in_week = getDayName(current_date.get(Calendar.DAY_OF_WEEK));
        Integer this_day = current_date.get(Calendar.DAY_OF_YEAR);
        Integer old_day = last_acces.get(Calendar.DAY_OF_YEAR);
        Integer dayDiffrence = this_day - old_day;
        mRepo.updateCoffeeCount(day_in_week, mRepo.getUser().getValue().getTodaysCoffees());
        Integer day_for_change;
        this_day = current_date.get(Calendar.DAY_OF_WEEK);
        for(day_for_change = this_day-1; dayDiffrence > 1; --dayDiffrence){
            if(day_for_change == 0) day_for_change = 7;
            mRepo.updateCoffeeCount(getDayName(day_for_change),0);
            --day_for_change;
        }
    }

}
