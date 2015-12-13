/*
 * Clover - 4chan browser https://github.com/Floens/Clover/
 * Copyright (C) 2014  Floens
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.trashtier.chan.chan;

import java.util.Locale;

public class ChanUrls {
    private static String scheme;

    public static void loadScheme(boolean useHttps) {
        scheme = useHttps ? "https" : "http";
    }

    public static String getCatalogUrl(String board) {
        return scheme + "://55chan.org/" + board + "/catalog.json";
    }

    public static String getPageUrl(String board, int pageNumber) {
        return scheme + "://55chan.org/" + board + "/" + pageNumber + ".json";
    }

    public static String getThreadUrl(String board, int no) {
        return scheme + "://55chan.org/" + board + "/res/" + no + ".json";
    }

    public static String getCaptchaSiteKey() {
        return "6Ldp2bsSAAAAAAJ5uyx_lx34lJeEpTLVkP5k04qc";
    }

    public static String getImageUrl(String board, String code, String extension) {
        return scheme + "://55chan.org/" + board + "/src/" + code + "." + extension;
    }

    public static String getThumbnailUrl(String board, String code) {
        return scheme + "://55chan.org/" + board + "/thumb/" + code + "s.jpg";
    }

    public static String getSpoilerUrl() {
        return scheme + "://55chan.org/static/spoiler.png";
    }

    public static String getCustomSpoilerUrl(String board, int value) {
        return scheme + "://s.4cdn.org/image/spoiler-" + board + value + ".png";
    }

    public static String getCountryFlagUrl(String countryCode) {
        return scheme + "://55chan.org/static/flags/" + countryCode.toLowerCase(Locale.ENGLISH) + ".png";
    }

    public static String getBoardsUrl() {
        return scheme + "://55chan.org/boards.json"; // not a thing in 55chan
    }

    public static String getReplyUrl(String board) {
        return "https://55chan.org/altpost.php";
    }

    public static String getDeleteUrl(String board) {
        //return "https://sys.4chan.org/" + board + "/imgboard.php";]
        return getReplyUrl(board);
    }

    public static String getBoardUrlDesktop(String board) {
        return scheme + "://55chan.org/" + board + "/";
    }

    public static String getThreadUrlDesktop(String board, int no) {
        return scheme + "://55chan.org/" + board + "/res/" + no + ".html";
    }

    public static String getThreadUrlDesktop(String board, int no, int postNo) {
        return scheme + "://55chan.org/" + board + "/res/" + no + ".html#" + postNo;
    }

    public static String getCatalogUrlDesktop(String board) {
        return scheme + "://55chan.org/" + board + "/catalog.html";
    }

    public static String getPassUrl() {
        return "https://sys.4chan.org/auth";
    }

    public static String getReportUrl(String board, int no) {
        //return "https://sys.4chan.org/" + board + "/imgboard.php?mode=report&no=" + no;

        return ""; // TODO: implement
    }
}
