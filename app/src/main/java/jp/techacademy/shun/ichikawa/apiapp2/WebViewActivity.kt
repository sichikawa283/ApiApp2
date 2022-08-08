package jp.techacademy.shun.ichikawa.apiapp2

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import io.realm.Realm
import io.realm.RealmConfiguration
import kotlinx.android.synthetic.main.activity_web_view.*

// クーポン詳細画面の動作
class WebViewActivity : AppCompatActivity(), FragmentCallback {
    private val viewPagerAdapter by lazy { ViewPagerAdapter(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_view)

        // realmバージョン差異で追加必要
        val realmConfiguration = RealmConfiguration.Builder()
            .allowWritesOnUiThread(true)
            .build()
        Realm.setDefaultConfiguration(realmConfiguration)
        var realm = Realm.getDefaultInstance()

        var couponUrls = CouponUrls(intent.getStringExtra(KEY_URL).toString(),
            intent.getStringExtra(KEY_URL).toString())
        var data = Shop(intent.getStringExtra(KEY_ID).toString(),
            couponUrls,
            intent.getStringExtra(KEY_IMAGE_URL).toString(),
            intent.getStringExtra(KEY_NAME).toString(),
            intent.getStringExtra(KEY_ADDRESS).toString())
        //webView.loadUrl(intent.getStringExtra(KEY_URL).toString())

        favoriteImageView.apply {
            // Picassoというライブラリを使ってImageVIewに画像をはめ込む
            setImageResource(if (FavoriteShop.findBy(intent.getStringExtra(KEY_ID)
                    .toString()) != null
            ) R.drawable.ic_star else R.drawable.ic_star_border)

            // お気に入りボタンをクリックした際の動作
            setOnClickListener {
                if (FavoriteShop.findBy(intent.getStringExtra(KEY_ID).toString()) != null) {
                    showConfirmDeleteFavoriteDialog(intent.getStringExtra(KEY_ID).toString())

                } else {
                    onAddFavorite(data)
                }
            }
        }
    }

    override fun onClickItem(
        url: String,
        id: String,
        name: String,
        imageURL: String,
        address:String
    ) {
        TODO("Not yet implemented")
    }

    // お気に入り登録した際の動作
    override fun onAddFavorite(shop: Shop) {
        FavoriteShop.insert(FavoriteShop().apply {
            id = shop.id
            name = shop.name
            imageUrl = shop.logoImage
            url = if (shop.couponUrls.sp.isNotEmpty()) shop.couponUrls.sp else shop.couponUrls.pc
        })
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    // お気に入り削除した際の動作
    override fun onDeleteFavorite(id: String) {
        showConfirmDeleteFavoriteDialog(id)
    }

    // お気に入り削除する際の確認のダイアログを表示させる
    private fun showConfirmDeleteFavoriteDialog(id: String) {
        AlertDialog.Builder(this)
            .setTitle(R.string.delete_favorite_dialog_title)
            .setMessage(R.string.delete_favorite_dialog_message)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                deleteFavorite(id)
            }
            .setNegativeButton(android.R.string.cancel) { _, _ -> }
            .create()
            .show()
    }

    // お気に入り削除する
    private fun deleteFavorite(id: String) {
        FavoriteShop.delete(id)
        // TODO: idが受け取れてない
        (viewPagerAdapter.fragments[MainActivity.VIEW_PAGER_POSITION_API] as ApiFragment).updateView()
        (viewPagerAdapter.fragments[MainActivity.VIEW_PAGER_POSITION_FAVORITE] as FavoriteFragment).updateData()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    companion object {
        private const val KEY_URL = "key_url"
        private const val KEY_ID = "key_id"
        private const val KEY_NAME = "key_name"
        private const val KEY_IMAGE_URL = "key_image_url"
        private const val KEY_ADDRESS = "key_address"

        fun start(
            activity: Activity,
            url: String,
            id: String,
            name: String,
            imageURL: String,
            address: String,
        ) {
            var intent = Intent(activity, WebViewActivity::class.java).putExtra(KEY_URL, url)
            intent.putExtra(KEY_ID, id)
            intent.putExtra(KEY_NAME, name)
            intent.putExtra(KEY_IMAGE_URL, imageURL)
            intent.putExtra(KEY_ADDRESS, address)

            activity.startActivity(intent)
        }
    }
}