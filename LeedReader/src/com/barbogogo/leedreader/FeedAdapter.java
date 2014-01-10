package com.barbogogo.leedreader;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.leed.reader.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class FeedAdapter extends ArrayAdapter<String>
{
    private static Context            mainContext;

    private static ArrayList<Article> articles;

    private static int                pPosition;

    final int                         pTitlePosition  = 0;
    final int                         pNoReadPosition = 1;

    private DataManagement            dataManagement;

    DisplayImageOptions               options;

    public FeedAdapter(Context context, Flux feed, DataManagement lDataManagement)
    {
        super(context, R.layout.activity_main, feed.getArticlesTitle());

        articles = feed.getArticles();

        mainContext = context;

        dataManagement = lDataManagement;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent)
    {
        LayoutInflater inflater =
                (LayoutInflater) mainContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View pRowView = inflater.inflate(R.layout.activity_feed, parent, false);

        TextView titleView = (TextView) pRowView.findViewById(R.id.articleTitle);
        TextView infoView = (TextView) pRowView.findViewById(R.id.articleInformation);
        TextView extractView = (TextView) pRowView.findViewById(R.id.articleExtract);
        // ImageView imageView = (ImageView)
        // pRowView.findViewById(R.id.articleImage);

        ImageView favoriteView = (ImageView) pRowView.findViewById(R.id.favorite);
        ImageView noReadView = (ImageView) pRowView.findViewById(R.id.readNoRead);

        int isRead = articles.get(position).getIsRead();
        int isFav = articles.get(position).getIsFav();

        titleView.setText(articles.get(position).getTitle());
        infoView.setText(articles.get(position).getDate() + " par " + articles.get(position).getAuthor()
                + " (" + articles.get(position).getNameFeed() + ")");
        extractView.setText(Utils.extractArticle(articles.get(position).getContent(), 20));
        // imageView.setImageBitmap(Utils.extractImage(mainContext,
        // articles.get(position).getContent()));

        // ImageLoader imageLoader = ImageLoader.getInstance();
        // imageLoader.clearMemoryCache();
        // imageLoader.clearDiscCache();
        // imageLoader.displayImage(Utils.extractImage(mainContext,
        // articles.get(position).getContent()),
        // imageView, options, null);

        favoriteView.setImageResource(R.drawable.fav_false);

        if (isRead == 1)
        {
            noReadView.setImageResource(R.drawable.read_true);
        }
        else
        {
            noReadView.setImageResource(R.drawable.read_false);
        }

        if (isFav == 1)
        {
            favoriteView.setImageResource(R.drawable.fav_true);
        }
        else
        {
            favoriteView.setImageResource(R.drawable.fav_false);
        }

        View.OnClickListener onClick = new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                pPosition = position;

                ((LeedReader) mainContext).setPosNavigation(LeedReader.cpArticle);

                setReadArticle(pPosition);

                ((LeedReader) mainContext).setModeView(LeedReader.cModeWebView);
                final WebviewAdapter adapter = new WebviewAdapter(articles, dataManagement);
                ViewPager myPager =
                        (ViewPager) ((LeedReader) mainContext).findViewById(R.id.home_pannels_pager);

                myPager.setOnPageChangeListener(new OnPageChangeListener()
                {

                    @Override
                    public void onPageSelected(int position)
                    {
                        setReadArticle(position);
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onPageScrollStateChanged(int arg0)
                    {
                    }

                    @Override
                    public void onPageScrolled(int arg0, float arg1, int arg2)
                    {
                    }
                });

                myPager.setAdapter(adapter);
                myPager.setCurrentItem(pPosition);
            }

            public void setReadArticle(int position)
            {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, articles.get(position).getTitle());
                shareIntent.putExtra(Intent.EXTRA_TEXT, articles.get(position).getTitle() + " - "
                        + articles.get(position).getUrlArticle());
                ((LeedReader) mainContext).doShare(shareIntent);

                if (articles.get(position).getIsRead() == 0)
                {
                    dataManagement.setReadArticle(articles.get(position), 1);
                    articles.get(position).setRead();

                    dataManagement.decreaseNbNoReadArticle(articles.get(position).getIdFeed());

                    notifyDataSetChanged();
                }
            }

        };

        pRowView.setOnClickListener(onClick);

        titleView.setOnClickListener(onClick);

        noReadView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                pPosition = position;

                Article article = articles.get(pPosition);

                if (article.getIsRead() == 0)
                {
                    article.setRead();
                    dataManagement.setReadArticle(article, 1);
                    dataManagement.decreaseNbNoReadArticle(article.getIdFeed());
                }
                else
                {
                    article.setUnRead();
                    dataManagement.setUnReadArticle(article);
                }

                notifyDataSetChanged();
            }
        });

        favoriteView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                pPosition = position;

                Article article = articles.get(pPosition);

                if (article.getIsFav() == 0)
                {
                    article.setFav(1);
                    dataManagement.setFavArticle(article);
                }
                else
                {
                    article.setFav(0);
                    dataManagement.setUnFavArticle(article);
                }

                notifyDataSetChanged();
            }
        });

        return pRowView;
    }
}
