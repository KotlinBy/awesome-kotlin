package link.kotlin.scripts

import link.kotlin.scripts.dsl.Article
import link.kotlin.scripts.dsl.ArticleFeature
import link.kotlin.scripts.dsl.ArticleFeature.highlightjs
import link.kotlin.scripts.dsl.ArticleFeature.mathjax
import link.kotlin.scripts.dsl.LinkType.article
import link.kotlin.scripts.dsl.LanguageCodes.EN
import link.kotlin.scripts.utils.writeFile
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

interface PagesGenerator {
    fun generate(articles: List<Article>, dist: String)

    companion object
}

private class DefaultPagesGenerator : PagesGenerator {
    override fun generate(articles: List<Article>, dist: String) {
        val articleBody = articles
            .groupBy { formatDate(it.date) }
            .map { """<div>${it.key}</div><ul>${getGroupLinks(it.value)}</ul>""" }
            .joinToString(separator = "\n")

        val article = Article(
            title = "Articles Index",
            url = "https://kotlin.link/articles/",
            body = articleBody,
            author = "Ruslan Ibragimov",
            date = LocalDate.now(),
            type = article,
            categories = listOf("Kotlin"),
            features = listOf(),
            filename = "index.html",
            lang = EN,
            description = articleBody
        )

        (articles + article).forEach {
            writeFile("$dist/articles/${it.filename}", getHtml(it))
        }
    }
}

private fun getGroupLinks(group: List<Article>): String {
    return group.joinToString(separator = "\n") {
        """<li><a href="./${it.filename}">${it.title}</a></li>"""
    }
}

internal val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
private fun formatDate(date: LocalDate): String {
    return date.format(formatter)
}

private fun formatDateTime(date: LocalDate): String {
    return date.format(DateTimeFormatter.ISO_DATE) + "T12:00:00+0000"
}

private fun getHtml(article: Article): String {
    return """
<!DOCTYPE html>
<html lang="${article.lang.name}">
<head>
    <meta charset="utf-8">
    <title>${article.title} – Kotlin.Link</title>
    <meta name="viewport"
          content="width=device-width, initial-scale=1.0, minimum-scale=1.0, maximum-scale=1.0, user-scalable=no"/>
    <meta name="format-detection" content="telephone=no"/>
    <meta http-equiv="X-UA-Compatible" content="IE=edge"/>
    <meta name="MobileOptimized" content="176"/>
    <meta name="HandheldFriendly" content="True"/>
    <meta name="robots" content="index, follow"/>
    <meta property="og:site_name" content="Kotlin.Link">
    <meta property="og:type" content="article">
    <meta property="og:title" content="${article.title}">
    <meta property="og:description" content="">
    <meta property="og:image" content="">
    <meta property="article:published_time" content="${formatDateTime(article.date)}">
    <meta property="article:modified_time" content="${formatDateTime(article.date)}">
    <meta property="article:author" content="${article.author}">
    <meta name="twitter:card" content="summary">
    <meta name="twitter:title" content="${article.title}">
    <meta name="twitter:description" content="">
    <meta name="twitter:image" content="">
    <link rel="shortcut icon" href="/favicon.ico" type="image/x-icon">
    <link rel="canonical" href="${article.url}"/>
    <link rel="alternate" type="application/rss+xml" title="Kotlin.Link - 20 latest" href="/rss.xml"/>
    <link rel="alternate" type="application/rss+xml" title="Kotlin.Link - full archive" href="/rss-full.xml"/>
    <link href="/styles.css" rel="stylesheet">
    ${getFeatures(article.features)}
</head>
<body>
<div class="kt_page_wrap">
    <div class="kt_page">
        <main class="kt_article">
            <header class="kt_article_header">
                <h1 dir="auto">${article.title}</h1>
                <address dir="auto">
                    <a href="${article.url}" rel="author" title="Article Origin">${article.author}</a>
                    <time datetime="${formatDateTime(article.date)}">${formatDate(article.date)}</time>
                </address>
            </header>
            <article id="_kt_editor" class="kt_article_content">
                <h1>${article.title}<br></h1>
                <address>${article.author}<br></address>
                ${article.body}
                <hr/>
            </article>
            <aside class="kt_article_buttons">
                <a href="/articles/" class="button edit_button">Articles</a>
            </aside>
        </main>
    </div>
</div>
</body>
</html>
"""
}

fun getFeatures(features: List<ArticleFeature>): String {
    return features.joinToString(separator = "\n") {
        when (it) {
            mathjax -> """<script src="https://cdn.mathjax.org/mathjax/latest/MathJax.js?config=TeX-AMS-MML_HTMLorMML"></script>"""
            highlightjs -> """
                <link rel="stylesheet" href="/github.css">
                <script src="/highlight.min.js"></script>
                <script>hljs.highlightAll();</script>
                """
        }
    }
}

fun PagesGenerator.Companion.default(): PagesGenerator {
    return DefaultPagesGenerator()
}
