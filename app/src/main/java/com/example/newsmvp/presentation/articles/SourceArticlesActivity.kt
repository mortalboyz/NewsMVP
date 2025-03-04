package com.example.newsmvp.presentation.articles

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.View.*
import android.widget.SearchView
import com.example.newsmvp.NewsApp
import com.example.newsmvp.R
import com.example.newsmvp.data.entities.Article
import com.example.newsmvp.external.AppSchedulerProvider
import com.example.newsmvp.external.SchedulerProvider
import com.example.newsmvp.presentation.adapter.NewsAdapter
import com.example.newsmvp.presentation.common.navigationcontroller.ActivityNavigation
import com.example.newsmvp.presentation.newssources.NewsSourcesActivity.Companion.TAG_SOURCE_ID
import com.example.newsmvp.presentation.newssources.NewsSourcesActivity.Companion.TAG_SOURCE_NAME
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.activity_source_articles.*
import kotlinx.android.synthetic.main.toolbar_activity.*
import javax.inject.Inject

class SourceArticlesActivity : AppCompatActivity(), SourceArticlesContract.View {

    @Inject lateinit var mPresenter: SourceArticlePresenter
    private lateinit var mAdapter: NewsAdapter
    private lateinit var mActivityNavigation: ActivityNavigation

    private var sourceId = ""
    private var sourceName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_source_articles)
//        (application as NewsApp).appComponent.inject(this)

        mPresenter.setView(this)

        val bundle = intent.extras
        if (bundle != null) {
            sourceId = bundle.getString(TAG_SOURCE_ID, "")
            sourceName = bundle.getString(TAG_SOURCE_NAME, "Articles")
        }
        setupUI()
        setupToolbar(sourceName)
        initializeData(sourceId)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        mPresenter.cancelFetchArticles()
    }


    override fun setupToolbar(sourceName: String) {
        btnToolbarBack.visibility = VISIBLE
        searchView.visibility = VISIBLE
        tvToolbarTitle.text = getString(R.string.source_articles, sourceName)
        btnToolbarBack.setOnClickListener { onBackPressed() }
        searchView.setOnCloseListener {
            tvToolbarTitle.visibility = VISIBLE
            btnToolbarBack.visibility = VISIBLE
            false
        }

        searchView.setOnSearchClickListener {
            tvToolbarTitle.visibility = GONE
            btnToolbarBack.visibility = GONE
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String?): Boolean {
                mPresenter.searchArticlesByTitle(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                mPresenter.searchArticlesByTitle(newText)
                return true
            }
        })
    }

    override fun setRecyclerView() {
        mAdapter = NewsAdapter(
            this,
            NewsAdapter.DATA_TYPE_ARTICLE,
            arrayListOf<Article>(),
            object : NewsAdapter.ListenerArticle {
                override fun onClickArticleItem(articleUrl: String, articleTitle: String?) {
                    mActivityNavigation.navigateToWebView(articleUrl, articleTitle ?: getString(R.string.web_view))
                }
            })
        rvSourceArticleList.adapter = mAdapter
    }

    override fun initializeData(sourceId: String) {
        mPresenter.fetchArticlesBySource(sourceId)
    }

    override fun setNavigation() {
    }

    override fun setupUI() {
        setNavigation()
        setRecyclerView()
    }

    override fun showProgressBar() {
        loadingIndicator.visibility = VISIBLE
    }

    override fun hideProgressBar() {
        loadingIndicator.visibility = GONE
    }

    override fun setArticles(articles: List<Article>?) {
        mAdapter.setList(articles)
    }

    override fun showNoSearchResult(query: String?) {
        layoutNoData.visibility = VISIBLE
        tvNoResult.text = getString(R.string.no_result_search, query)
    }

    override fun hideNoSearchResult() {
        layoutNoData.visibility = GONE
    }
}
