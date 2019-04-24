package org.dexon;

public interface OnCompleteListener<T extends Request> {
    void onComplete(Response<T> response);
}
