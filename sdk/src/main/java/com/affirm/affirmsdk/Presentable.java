package com.affirm.affirmsdk;

public interface Presentable<I> {
  void onAttach(I page);

  void onDetach();
}
