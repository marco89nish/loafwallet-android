
package com.breadwallet.presenter.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.breadwallet.R;
import com.breadwallet.presenter.fragments.IntroNewRecoverFragment;
import com.breadwallet.presenter.fragments.IntroNewWalletFragment;
import com.breadwallet.presenter.fragments.IntroRecoverWalletFragment;
import com.breadwallet.presenter.fragments.IntroWarningFragment;
import com.breadwallet.presenter.fragments.IntroWelcomeFragment;
import com.breadwallet.tools.WordsReader;
import com.breadwallet.tools.sqlite.MerkleBlockDataSource;
import com.breadwallet.tools.sqlite.TransactionDataSource;
import com.breadwallet.tools.sqlite.entities.BRMerkleBlockEntity;
import com.breadwallet.tools.sqlite.entities.BRTransactionEntity;
import com.breadwallet.tools.sqlite.entities.BRTxInputEntity;
import com.breadwallet.tools.sqlite.entities.BRTxOutputEntity;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


/**
 * BreadWallet
 * <p/>
 * Created by Mihail on 8/4/15.
 * Copyright (c) 2015 Mihail Gutan <mihail@breadwallet.com>
 * <p/>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

public class IntroActivity extends FragmentActivity {
    public static final String TAG = IntroActivity.class.getName();
    ImageView background;
    RelativeLayout layout;
    boolean noWallet = true;
    private static final int RightToLeft = 1;
    private static final int LeftToRight = 2;
    private static final int DURATION = 30000;

    private ValueAnimator mCurrentAnimator;
    private final Matrix mMatrix = new Matrix();
    private float mScaleFactor;
    private int mDirection = RightToLeft;
    private RectF mDisplayRect = new RectF();
    private IntroWelcomeFragment introWelcomeFragment;
    private IntroNewRecoverFragment introNewRestoreFragment;
    private IntroNewWalletFragment introNewWalletFragment;
    private IntroWarningFragment introWarningFragment;
    private IntroRecoverWalletFragment introRecoverWalletFragment;
    private Button leftButton;
    private boolean backPressAvailable = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_intro);

        Log.e(TAG, "Activity created!");
        if (savedInstanceState != null) {
            return;
        }

//        testSQLiteConnectivity(this);   //do some sqlite testing
        introWelcomeFragment = new IntroWelcomeFragment();
        introNewRestoreFragment = new IntroNewRecoverFragment();
        introNewWalletFragment = new IntroNewWalletFragment();
        introWarningFragment = new IntroWarningFragment();
        introRecoverWalletFragment = new IntroRecoverWalletFragment();
        leftButton = (Button) findViewById(R.id.intro_left_button);

        background = (ImageView) findViewById(R.id.intro_bread_wallet_image);
        background.setScaleType(ImageView.ScaleType.MATRIX);
        leftButton.setVisibility(View.GONE);
        leftButton.setClickable(false);

        background.post(new Runnable() {
            @Override
            public void run() {
                mScaleFactor = (float) background.getHeight() /
                        (float) background.getDrawable().getIntrinsicHeight();
                mMatrix.postScale(mScaleFactor, mScaleFactor);
                background.setImageMatrix(mMatrix);
                animate();
            }
        });

        leftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        getSupportFragmentManager().beginTransaction().add(R.id.intro_layout, introWelcomeFragment,
                "introWelcomeFragment").commit();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (noWallet) {
                    Log.e(TAG, "should create new wallet");
                    showRecoverNewWalletFragment();
                } else {
                    Log.e(TAG, "should go to the current wallet");
                    startMainActivity();
                }

            }
        }, 1200);

        //testing plist:

        List<String> wordList = null;
        try {
            wordList = WordsReader.getWordList(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
//
//        Iterator<String> it = wordList.iterator();
//        int count = 0;
//        while (it.hasNext()) {
//            System.out.println(it.next() + (++count));
//
//        }

        //end testing

    }

    void showRecoverNewWalletFragment() {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.setCustomAnimations(R.animator.from_right, R.animator.to_left);
        fragmentTransaction.replace(introWelcomeFragment.getId(), introNewRestoreFragment);
        fragmentTransaction.commit();
    }

    public void showNewWalletFragment() {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.addToBackStack(null);
        leftButton.setVisibility(View.VISIBLE);
        leftButton.setClickable(true);
        fragmentTransaction.setCustomAnimations(R.animator.from_right, R.animator.to_left);
        fragmentTransaction.replace(introNewRestoreFragment.getId(), introNewWalletFragment);
        fragmentTransaction.commit();
        backPressAvailable = true;
    }

    public void showRecoverWalletFragment() {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.addToBackStack(null);
        leftButton.setVisibility(View.VISIBLE);
        leftButton.setClickable(true);
        fragmentTransaction.setCustomAnimations(R.animator.from_right, R.animator.to_left);
        fragmentTransaction.replace(introNewRestoreFragment.getId(), introRecoverWalletFragment);
        fragmentTransaction.commit();
        backPressAvailable = true;
    }

    public void showWarningFragment() {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(introNewWalletFragment.getId(), introWarningFragment);
        introNewWalletFragment.introGenerate.setClickable(false);
        leftButton.setVisibility(View.GONE);
        leftButton.setClickable(false);
        fragmentTransaction.commit();
        backPressAvailable = false;
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void animate() {
        updateDisplayRect();
        if (mDirection == RightToLeft) {
            animate(mDisplayRect.left, mDisplayRect.left -
                    (mDisplayRect.right - background.getWidth()));
        } else {
            animate(mDisplayRect.left, 0.0f);
        }
    }

    private void animate(float from, float to) {
        mCurrentAnimator = ValueAnimator.ofFloat(from, to);
        mCurrentAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (Float) animation.getAnimatedValue();

                mMatrix.reset();
                mMatrix.postScale(mScaleFactor, mScaleFactor);
                mMatrix.postTranslate(value, 0);

                background.setImageMatrix(mMatrix);

            }
        });
        mCurrentAnimator.setDuration(DURATION);
        mCurrentAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (mDirection == RightToLeft)
                    mDirection = LeftToRight;
                else
                    mDirection = RightToLeft;

                animate();
            }
        });
        mCurrentAnimator.start();
    }

    private void updateDisplayRect() {
        mDisplayRect.set(0, 0, background.getDrawable().getIntrinsicWidth(),
                background.getDrawable().getIntrinsicHeight());
        mMatrix.mapRect(mDisplayRect);
    }

    public void startMainActivity() {
        Intent intent;
        intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        if (!IntroActivity.this.isDestroyed()) {
            finish();
        }
    }

    public void startIntroShowPhrase() {
        Intent intent;
        intent = new Intent(this, IntroShowPhraseActivity.class);
        startActivity(intent);
        if (!IntroActivity.this.isDestroyed()) {
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() == 1) {
            leftButton.setVisibility(View.GONE);
            leftButton.setClickable(false);
        }
        if (backPressAvailable)
            super.onBackPressed();

    }

    public void testSQLiteConnectivity(Activity context) {
        // Test MerkleBlock Table
        BRMerkleBlockEntity merkleBlockEntity = new BRMerkleBlockEntity();
        String blockHash = "000000000019d6689c085ae165831e934ff763ae46a2a6c172b3f1b60a8ce26f";
        merkleBlockEntity.setId(21);
        merkleBlockEntity.setBlockHash(blockHash.getBytes());
        merkleBlockEntity.setFlags("someFLags".getBytes());
        merkleBlockEntity.setHashes("someHashes".getBytes());
        merkleBlockEntity.setHeight(12312);
        merkleBlockEntity.setMerkleRoot("merkleRoot".getBytes());
        merkleBlockEntity.setNonce(6363);
        merkleBlockEntity.setPrevBlock("prevBlock".getBytes());
        merkleBlockEntity.setTarget(423423423);
        merkleBlockEntity.setTimeStamp(423211244);
        merkleBlockEntity.setTotalTransactions(511);
        merkleBlockEntity.setVersion(5);

        MerkleBlockDataSource MBdataSource;
        MBdataSource = new MerkleBlockDataSource(this);
        MBdataSource.open();
        MBdataSource.createMerkleBlock(merkleBlockEntity);
        List<BRMerkleBlockEntity> values = MBdataSource.getAllMerkleBlocks();
        Iterator<BRMerkleBlockEntity> merkleBlockEntityIterator = values.iterator();
        while (merkleBlockEntityIterator.hasNext()) {
            BRMerkleBlockEntity tmp = merkleBlockEntityIterator.next();
            Log.e(TAG, "The merkleBlock: " + tmp.getId() + " " + tmp.getBlockHash() + " " + tmp.getFlags() +
                    " " + tmp.getHashes() + " " + tmp.getHeight() + " " + tmp.getMerkleRoot() + " " +
                    tmp.getNonce() + " " + tmp.getPrevBlock() + " " + tmp.getTarget() + " " +
                    tmp.getTimeStamp() + tmp.getTotalTransactions() + " " + tmp.getVersion());

        }

        // Test Transaction Table
        BRTxInputEntity input1 = new BRTxInputEntity();
        input1.setTxHash("somehash".getBytes());
        input1.setId(123);
        input1.setSequence(23123123);
        input1.setSignatures("976sd56ds56gds5fsdfd67fsd697".getBytes());

        BRTxInputEntity input2 = new BRTxInputEntity();
        input1.setTxHash("somehash2".getBytes());
        input1.setId(55);
        input1.setSequence(31124124);
        input1.setSignatures("98sdf78ds67f6sd87f68sd7".getBytes());

        HashSet<BRTxInputEntity> inputs = new HashSet<>();
        inputs.add(input1);
        inputs.add(input2);

        BRTransactionEntity transactionEntity = new BRTransactionEntity();
        String txHash = "000000000019d6689c085ae165831e934ff763ae46a2a6c172b3f1b60a8ce26f";
        transactionEntity.setId(98);
        transactionEntity.setBlockHeight(123123);
        transactionEntity.setTimeStamp(97986716);
        transactionEntity.setLockTime(232131231);
        transactionEntity.setTxHash(txHash.getBytes());
        transactionEntity.setOutputs(new HashSet<BRTxOutputEntity>());
        transactionEntity.setInputs(inputs);

        TransactionDataSource TXdataSource = new TransactionDataSource(this);
        TXdataSource.open();
        TXdataSource.createTransaction(transactionEntity);
        List<BRTransactionEntity> txValues = TXdataSource.getAllTransactions();
        Iterator<BRTransactionEntity> transactionEntityIterator = txValues.iterator();
        while (transactionEntityIterator.hasNext()) {
            BRTransactionEntity transactionEntity1 = transactionEntityIterator.next();
            Log.e(TAG, "The transaction: " + transactionEntity1.getId() + " " + transactionEntity1.getBlockHeight() +
                    " " + transactionEntity1.getTimeStamp() +
                    " " + transactionEntity1.getLockTime() + " " + transactionEntity1.getTxHash());
            Set<BRTxInputEntity> inputsFromBytes = transactionEntity1.getInputs();
            for (BRTxInputEntity input : inputsFromBytes) {
                Log.e(TAG, "INPUTS: " + input.getTxHash() + " " + input.getSequence() + " " + input.getSignatures());
            }

        }
    }
}