package edu.wm.cs.cs425.postserviceapp.ui.staff_home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class StaffHomeViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public StaffHomeViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is staffHomefragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}