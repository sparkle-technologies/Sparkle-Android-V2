package com.cyberflow.sparkle.chat.ui.dialog;


import com.cyberflow.base.act.BaseVBAct;

public class SimpleDialogFragment extends DemoDialogFragment {
    public static final String MESSAGE_KEY = "message";

    @Override
    public void initArgument() {
        if (getArguments() != null) {
            title = getArguments().getString(MESSAGE_KEY);
        }
    }

    public static class Builder extends DemoDialogFragment.Builder {

        public Builder(BaseVBAct context) {
            super(context);
        }

        @Override
        protected DemoDialogFragment getFragment() {
            return new SimpleDialogFragment();
        }

    }


}
