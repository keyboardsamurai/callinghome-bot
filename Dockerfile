FROM alpine:latest

RUN apk add openjdk17-jre-headless

# Set the working directory in the container
WORKDIR /app

# Copy the locally compiled code to the container
COPY target/ .

ENV CHAT_ID="14112131"
ENV TELEGRAM_BOT_NAME="PlaceToLiveBot"
ENV TELEGRAM_BOT_TOKEN="5843074964:AAHZCTNVRfm5PvJcGe2gPCnZa9CQYQ03qI8"
ENV DENY_LIST="WBS,seniorentraum,befristet,brück,buchforst,buchheim,chorweiler,dellbrück,dünnwald,elsdorf,finkenberg,flittard,fühlingen,gremberg,gremberghoven,grengel,heumar,holweide,humboldt,höhenberg,höhenhaus,kalk,karneval,kellerwohnung,langel,libur,lind,merheim,merkenich,meschenich,möbiliert,möbliert,mülheim,ostheim,poll,porz,rhodenkirchen,rodenkirchen,seeberg,stammheim,suerth,sürth,tauschangebot,tauschwohnung,urbach,vingst,wahn,wahnheide,westhoven,wg zimmer,wg-zimmer,worringen,zeitwohnung,zwischenmiete,zündorf"
ENV EBAY_KLEINANZEIGEN_URL="https://www.ebay-kleinanzeigen.de/s-wohnung-mieten/koeln/anzeige:angebote/preis::1100/wohnung-mieten/k0c203l945+wohnung_mieten.qm_d:60.00%2C+wohnung_mieten.zimmer_d:2.0%2C"
ENV INTERVAL="10"

# Run the app
CMD ["java", "-jar", "app.jar"]
