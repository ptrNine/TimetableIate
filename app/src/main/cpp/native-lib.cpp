#include <jni.h>
#include <string>
#include <vector>

extern "C" JNIEXPORT jstring JNICALL
Java_com_timetable_slava_timetableiate_GetTimetableAsyncTask_getCsrfToken(
        JNIEnv* env,
        jobject obj,
        jstring html )
{
    std::string html_data = env->functions->GetStringUTFChars(env, html, 0);

    // Hardcode-style
    auto csrfStart = html_data.find("csrf-token");
    csrfStart = html_data.find("content=\"", csrfStart);
    csrfStart += 9;
    auto csrfEnd = html_data.find("\"", csrfStart);

    return env->NewStringUTF(html_data.substr(csrfStart, csrfEnd - csrfStart).c_str());

}


std::string getVal(const char* val, const std::string& where) {
    // val name entry
    auto entry_first = where.find(val);
    if (entry_first == std::string::npos)
        entry_first = 0;


    // end
    auto entry_last = where.find(";", entry_first);
    if (entry_last == std::string::npos)
        entry_last = entry_first;

    return where.substr(entry_first, entry_last - entry_first);
}


class TimetableLesson {
public:
    std::string time;
    std::string type;
    std::string name;
    std::string parity;
    std::vector<std::pair<std::string, std::string>> parameter1;
    std::vector<std::pair<std::string, std::string>> parameter2;
};



class TimetableDay {
public:
    std::string name;

public:
    TimetableDay() {}
    TimetableDay(size_t count)           { _arr.resize(count); }
    TimetableLesson&  get     (size_t pos) { return _arr[pos]; }
    size_t          getCount()           { return _arr.size(); }

private:
    std::vector<TimetableLesson> _arr;
};



class Timetable {
public:
    std::string name;

public:
    Timetable(size_t count) {
        _arr.resize(count);
    }

    void           set(const TimetableDay& day, size_t num) { _arr[num] = day; }
    TimetableDay&  get(size_t pos)                          { return _arr[pos]; }

    void serialize(std::string& out) {
        out.clear();
        out.reserve(16834);

        out += "{\n\t\"name\" : \"";
        out += name;
        out += "\",\n\t\"data\" : [";



        for (size_t i = 0; i < _arr.size(); ++i) {
            auto& day = _arr[i];
            out += "\n\t{";
            out += "\n\t\t\"name\" : \"";
            out += day.name;
            out += "\",\n\t\t\"data\" : [";
            for (size_t j = 0; j < day.getCount(); ++j) {
                auto& item = day.get(j);
                out += "\n\t\t{\n\t\t\t\"name\" : \"";
                out += item.name;
                out += "\",\n\t\t\t\"type\" : \"";
                out += item.type;
                out += "\",\n\t\t\t\"time\" : \"";
                out += item.time;
                out += "\",\n\t\t\t\"parity\" : \"";
                out += item.parity;

                out += "\",\n\t\t\t\"parameter1\" : [";
                for (size_t k = 0; k < item.parameter1.size(); ++k) {
                    out += "\n\t\t\t{\n\t\t\t\t\"reference\" : \"";
                    out += item.parameter1[k].first;
                    out += "\",\n\t\t\t\t\"name\" : \"";
                    out += item.parameter1[k].second;
                    out += "\"\n\t\t\t}";
                    if (k != item.parameter1.size() - 1)
                        out += ",";
                }
                out += "],\n\t\t\t\"parameter2\" : [";
                for (size_t k = 0; k < item.parameter2.size(); ++k) {
                    out += "\n\t\t\t{\n\t\t\t\t\"reference\" : \"";
                    out += item.parameter2[k].first;
                    out += "\",\n\t\t\t\t\"name\" : \"";
                    out += item.parameter2[k].second;
                    out += "\"\n\t\t\t}";
                    if (k != item.parameter2.size() - 1)
                        out += ",";
                }

                out += "]\n\t\t}";
                if (j != day.getCount()-1)
                    out += ",";
            }
            out += "]";
            out += "\n\t}";
            if (i != _arr.size() - 1)
                out += ",";
        }
        out += "]\n}";
    }

private:
    std::vector<TimetableDay> _arr;
};




class TimetableParser {
public:
    size_t calcEntries(const char* val, const std::string& input) {
        size_t entries = 0;
        size_t last_pos = 0;
        size_t finder = input.find(val);

        while (finder != std::string::npos) {
            entries++;
            last_pos = finder + 1;
            finder = input.find(val, last_pos);
        }

        return entries;
    }

    void splitByEntries(const char* val, const std::string& input, std::vector<std::string>& result) {
        result.reserve(6);
        std::vector<size_t> position_vec;

        size_t entries = 0;
        size_t last_pos = 0;
        size_t finder = input.find(val);

        while (finder != std::string::npos) {
            position_vec.push_back(finder);
            entries++;
            last_pos = finder + 1;
            finder = input.find(val, last_pos);
        }
        position_vec.push_back(input.size() - 1);

        for (size_t i = 0; i < position_vec.size() - 1; ++i) {
            result.emplace_back(input.substr(position_vec[i], position_vec[i+1] - position_vec[i]));
        }
    }

    std::string readAfter(const char* after, size_t aftrlen, const char* before, const std::string& input) {
        size_t finder1 = input.find(after);
        size_t finder2 = input.find(before, finder1 + 1);

        if (finder1 != std::string::npos && finder2 != std::string::npos)
            return input.substr(finder1 + (aftrlen - 1), finder2 - finder1 - (aftrlen - 1));
        else
            return std::string();
    }

    std::pair<std::string, std::string> readAfterWithDelim(const char* after, size_t aftrlen, const char* before, const char* delim, size_t delim_len, const std::string& input) {
        std::string str = readAfter(after, aftrlen, before, input);
        size_t find = str.find(delim);

        if (find != std::string::npos)
            return std::make_pair(str.substr(0, find), str.substr(find + delim_len));
        else
            return std::make_pair(std::string(), std::string());
    }

    std::vector<std::pair<std::string, std::string>>
    readPairs(const char* after,
              size_t aftrlen,
              const char* afterIn,
              size_t aftrinlen,
              const char* before,
              const char* delim,
              size_t delim_len,
              const char* last,
              const std::string& input)
    {
        std::vector<std::pair<std::string, std::string>> result;
        size_t finder = input.find(after);
        size_t first = finder + aftrlen - 1;
        size_t lastFind = input.find(last, finder);

        while (finder != std::string::npos && finder < lastFind) {
            size_t second = input.find(delim, first);

            if (second == std::string::npos)
                break;

            result.push_back(std::make_pair(input.substr(first, second - first), std::string()));

            second += delim_len;
            size_t third = input.find(before, second);

            if (third == std::string::npos)
                break;

            result.back().second = input.substr(second, third - second);

            third++;
            finder = input.find(afterIn, third);
            first = finder + aftrinlen - 1;
        }

        return result;
    }
    std::string eraseDashInTime(std::string str) {
        if (str.size() > 12) {
            str.erase(5, 4);
            str[5] = ' ';
            str[6] = '-';
            str[7] = ' ';
        }
        return str;
    }


    void run(const std::string& input, std::string& output) {
        std::string temp;
        temp.reserve(input.size());


        for (size_t i = 0; i < input.size(); ++i) {
            //if (input[i] == '\n' || input[i] == '\t')
            //    continue;

            temp.push_back(input[i]);
            if (input[i] == '>') {
                auto imem = i;
                imem++;
                bool need_to_erase = true;
                while(imem < input.size() && input[imem] != '<') {
                    if (input[imem] != ' ' && input[imem] != '\t' && input[imem] != '\n') {
                        need_to_erase = false;
                        break;
                    }
                    imem++;
                }

                if (need_to_erase)  {
                    i = imem;
                    temp.push_back(input[i]);
                }
            }

        }

        //////////////////////
        //output = temp;

        std::vector<std::string> days;
        // split by days
        splitByEntries("day-of-week\">", temp, days);

        size_t finder1 = days.back().find("right-menu\">");
        if (finder1 != std::string::npos)
            days.back() = days.back().substr(0, finder1); // concat last day


        std::vector<std::vector<std::string>> lessons;
        lessons.resize(days.size());
        // split by lessons
        for (size_t i = 0; i < days.size(); ++i) {
            splitByEntries("row\">", days[i], lessons[i]);
        }

        Timetable timetable(days.size());
        timetable.name = readAfter("<span class=\"group-name\">", 26, "</span>", temp);

        for (size_t i = 0; i < lessons.size(); ++i) {
            TimetableDay timetableDay(lessons[i].size());

            timetableDay.name = readAfter("<strong>", 9, "</strong>", days[i]);

            for (size_t j = 0; j < lessons[i].size(); ++j) {
                timetableDay.get(j).time = eraseDashInTime(readAfter("col-xs-1\">", 11, "</div>", lessons[i][j]));
                timetableDay.get(j).type = readAfter("lesson-type\">", 14, "</div>", lessons[i][j]);
                timetableDay.get(j).parity = readAfter("lesson-week nopadding circle ", 30, "\"", lessons[i][j]);
                timetableDay.get(j).name = readAfter("lesson-name\">", 14, "</div>", lessons[i][j]);
                timetableDay.get(j).parameter1 = readPairs("col-xs-2\"><a href=\"", 20, "<a href=\"", 10, "</a>", "\">", 2, "</div>", lessons[i][j]);
                timetableDay.get(j).parameter2 = readPairs("col-xs\"><a href=\"", 18, "<a href=\"", 10, "</a>", "\">", 2, "</div>", lessons[i][j]);
            }

            timetable.set(timetableDay, i);
        }

        timetable.serialize(output);
    }
};


extern "C" JNIEXPORT jstring JNICALL
//Java_com_example_slava_myapplication11_TimetableActivity_parseTimetable(
Java_com_timetable_slava_timetableiate_MainActivity_parseTimetable(
        JNIEnv* env,
        jobject obj,
        jstring jHtmlPage )
{
    std::string htmlPage = env->functions->GetStringUTFChars(env, jHtmlPage, 0);
    std::string json;

    TimetableParser parser;
    parser.run(htmlPage, json);

    return env->NewStringUTF(json.c_str());
}